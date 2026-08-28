/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.settings.loginnotice

import com.fasterxml.jackson.databind.ObjectMapper
import com.tle.beans.Institution
import com.tle.common.filesystem.FileEntry
import com.tle.common.institution.CurrentInstitution
import com.tle.core.jackson.ObjectMapperService
import com.tle.core.services.FileSystemService
import com.tle.core.settings.loginnotice.impl.{
  LoginNoticeImageStore,
  LoginNoticeServiceImpl,
  PreLoginNotice
}
import com.tle.core.settings.service.ConfigurationService
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{mock, mockStatic, never, verify, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import javax.imageio.ImageIO
import javax.ws.rs.{BadRequestException, WebApplicationException}

class LoginNoticeServiceTest
    extends AnyFunSpec
    with Matchers
    with GivenWhenThen
    with BeforeAndAfterAll
    with TableDrivenPropertyChecks {

  private val fileSystemService: FileSystemService = mock(classOf[FileSystemService])
  when(fileSystemService.enumerate(any(), anyString(), any())).thenReturn(Array.empty[FileEntry])

  private val configurationService: ConfigurationService = mock(classOf[ConfigurationService])

  private val objectMapperService: ObjectMapperService = mock(classOf[ObjectMapperService])
  private val objectMapper                             = new ObjectMapper()
  when(objectMapperService.createObjectMapper()).thenReturn(objectMapper)

  private val loginNoticeService: LoginNoticeService =
    new LoginNoticeServiceImpl(configurationService, new LoginNoticeImageStore(fileSystemService)) {
      override def checkPermissions(): Unit = ()
      setObjectMapperService(objectMapperService)
    }

  override def beforeAll: Unit = {
    mockStatic(classOf[CurrentInstitution])
    val inst = new Institution
    inst.setUniqueId(2026L)
    inst.setUrl("https://example.com/institution/")
    when(CurrentInstitution.get()).thenReturn(inst)
  }

  describe("LoginNoticeServiceImpl.uploadPreLoginNoticeImage") {
    it("accepts a genuine image and returns its filename") {
      val acceptedFormats = Table(
        ("format", "filename"),
        ("png", "photo.png"),
        ("jpg", "photo.jpg"),
        ("gif", "photo.gif"),
        // jpeg is an alias of jpg, and the extension is matched case-insensitively.
        ("jpg", "photo.jpeg"),
        ("png", "photo.PNG"),
        // The extension is checked independently of the content, so a real image under the wrong
        // image extension is allowed.
        ("png", "photo.jpg")
      )

      forAll(acceptedFormats) { (format, filename) =>
        Given(s"a real $format file")
        val stream = toStream(encode(format))

        When("this file is uploaded")
        val result = loginNoticeService.uploadPreLoginNoticeImage(stream, filename)

        Then("the filename should be returned")
        result shouldBe filename
      }
    }

    it("rejects content that is not a genuine image") {
      val html = "<html><body><script>alert(document.cookie)</script></body></html>"
        .getBytes(StandardCharsets.UTF_8)

      val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
        .getBytes(StandardCharsets.UTF_8)

      // The first 33 bytes of a PNG are the signature plus a complete IHDR chunk, whatever the
      // image dimensions - so this is a well formed header with the pixel data cut off.
      val truncatedPng = encode("png").take(33)

      // The magic number of a real PNG in front of bytes that are not one.
      val pngSignatureWithJunk = encode("png").take(8) ++ Array.fill(100)('X'.toByte)

      val rejectedContent = Table(
        ("description", "content", "filename"),
        // Image filenames, so it is the content check that has to do the rejecting.
        ("HTML", html, "photo.png"),
        ("Scriptable SVG", svg, "photo.png"),
        ("Empty", Array.emptyByteArray, "photo.png"),
        ("Garbage bytes", Array[Byte](1, 2, 3, 4, 5, 6, 7, 8), "photo.png"),
        // Decodes cleanly, but BMP is not a format we accept, so the extension cannot smuggle it
        // in.
        ("BMP", encode("bmp"), "photo.png"),
        // A PNG reader is found for these and its MIME type is allowed, so the full decode is the
        // only check that can reject them.
        ("Truncated PNG", truncatedPng, "photo.png"),
        ("PNG signature followed by junk", pngSignatureWithJunk, "photo.png"),
        // Filenames that match the payload, so neither check has any reason to let them through.
        ("HTML named as HTML", html, "evil.html"),
        ("Scriptable SVG named as SVG", svg, "evil.svg")
      )

      forAll(rejectedContent) { (description, content, filename) =>
        Given(s"$description content uploaded as $filename")
        val stream = toStream(content)

        Then("uploading this file throws a BadRequestException")
        assertThrows[BadRequestException] {
          loginNoticeService.uploadPreLoginNoticeImage(stream, filename)
        }
      }
    }

    it("rejects a genuine image whose filename is not an image filename") {
      // Genuine PNG content, but stored under a name the extension check must refuse.
      val rejectedFilenames = Table(
        ("description", "filename"),
        ("a non-image extension", "photo.html"),
        ("a trailing extension after an image one", "photo.png.html"),
        ("no extension at all", "photo"),
        // ImageIO can read BMP, but the allowlist is narrower than the ImageIO registry on purpose.
        ("an image extension outside the allowlist", "photo.bmp")
      )

      forAll(rejectedFilenames) { (description, filename) =>
        Given(s"a real PNG file supplied with $description")
        val stream = toStream(encode("png"))

        Then("uploading this file throws a BadRequestException")
        assertThrows[BadRequestException] {
          loginNoticeService.uploadPreLoginNoticeImage(stream, filename)
        }
      }
    }
  }

  describe("LoginNoticeServiceImpl.setPreLoginNotice") {

    def noticeWithContent(content: String): PreLoginNotice = {
      val notice = new PreLoginNotice()
      notice.setNotice(content)
      notice.setStartDate(ZonedDateTime.now())
      notice.setEndDate(ZonedDateTime.now().plusDays(1))
      notice
    }

    it("rejects saving a notice with disallowed content") {
      Given("a notice containing a script tag")
      val notice = noticeWithContent("<div>ok</div><script>alert(1)</script>")

      Then("saving it throws an exception and the content is never persisted")
      assertThrows[WebApplicationException] {
        loginNoticeService.setPreLoginNotice(notice)
      }
      verify(configurationService, never()).setProperty(anyString(), anyString())
    }

    it("saves a clean notice without any disallowed content") {
      Given("a notice that is already clean")
      val input  = "<p>hello</p>"
      val notice = noticeWithContent(input)

      When("it is saved")
      loginNoticeService.setPreLoginNotice(notice)

      Then("the store is written to with the notice content unchanged")
      val persistedJson = ArgumentCaptor.forClass(classOf[String])
      verify(configurationService).setProperty(anyString(), persistedJson.capture())

      val saved = objectMapper.readValue(persistedJson.getValue, classOf[PreLoginNotice])
      saved.getNotice shouldBe input
    }
  }

  describe("LoginNoticeServiceImpl.getPreLoginNotice") {
    val PRE_LOGIN_NOTICE_KEY = "pre.login.notice"

    it("sanitises content that was stored before this sanitisation existed") {
      Given("a notice stored with a script tag")

      val uncleanNotice = new PreLoginNotice()
      uncleanNotice.setNotice("<div>ok</div><script>alert(1)</script>")
      when(configurationService.getProperty(PRE_LOGIN_NOTICE_KEY))
        .thenReturn(objectMapper.writeValueAsString(uncleanNotice))

      When("it is retrieved")
      val result = loginNoticeService.getPreLoginNotice

      Then("the script tag is stripped before it is returned")
      result.getNotice shouldBe "<div>ok</div>"
    }
  }

  private def toStream(bytes: Array[Byte]): InputStream = new ByteArrayInputStream(bytes)

  private def encode(format: String): Array[Byte] = {
    val image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB)
    val os    = new ByteArrayOutputStream()
    ImageIO.write(image, format, os)
    os.toByteArray
  }
}
