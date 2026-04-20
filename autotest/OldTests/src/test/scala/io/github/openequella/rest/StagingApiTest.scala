package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import com.tle.common.URLUtils
import com.tle.webtests.pageobject.AbstractPage
import com.tle.webtests.test.files.Attachments
import org.apache.commons.httpclient.methods._
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, NameValuePair}
import org.testng.Assert._
import org.testng.annotations.Test

import java.io.File
import scala.jdk.CollectionConverters._

class StagingApiTest extends AbstractRestApiTest {

  private val TestFile = "Special characters - хцч test2.jpg"

  private case class UploadedPart(partNumber: Int, etag: String)

  private val api = new StagingApi

  override def loginAsLowPrivilegeUser(): Unit =
    makeClientRequest(authHelper.buildLoginMethod("AutoTest_StagingLowPriv", "``````"))

  private def withStaging(testCode: String => Unit): Unit = {
    val (status, uuid) = api.createStaging()
    assertEquals(status, HttpStatus.SC_CREATED, "Staging creation failed")
    try testCode(uuid)
    finally api.deleteStaging(uuid)
  }

  @Test(description = "Guest users should not be able to access staging endpoints")
  def guestAccessDeniedTest(): Unit = withStaging { stagingUuid =>
    val file = getTestFile("avatar.png")
    logout()
    assertFalse(hasAuthenticatedSession, "Session should be guest after logout")

    assertEquals(api.createStaging()._1, HttpStatus.SC_FORBIDDEN)
    assertEquals(api.getStaging(stagingUuid)._1, HttpStatus.SC_FORBIDDEN)
    assertEquals(
      api.uploadFile(stagingUuid, "guest-upload.txt", file, None),
      HttpStatus.SC_FORBIDDEN
    )
    assertEquals(api.headFile(stagingUuid, "avatar.png")._1, HttpStatus.SC_FORBIDDEN)
    assertEquals(api.deleteFile(stagingUuid, "avatar.png"), HttpStatus.SC_FORBIDDEN)
    assertEquals(api.deleteStaging(stagingUuid), HttpStatus.SC_FORBIDDEN)

    login()
    assertTrue(hasAuthenticatedSession, "Authenticated session should be restored")
  }

  @Test(description = "Create an empty staging area")
  def createStagingTest(): Unit = withStaging { stagingUuid =>
    val (status, body) = api.getStaging(stagingUuid)
    assertEquals(status, HttpStatus.SC_OK)
    assertEquals(body.get("files").size(), 0, "New staging area should be empty")
  }

  @Test(description = "Upload a file to the staging area")
  def uploadFileTest(): Unit = withStaging { stagingUuid =>
    val file              = getTestFile(TestFile)
    val encodedTargetPath = "folder/" + URLUtils.urlEncode(TestFile, false)

    assertEquals(api.uploadFile(stagingUuid, encodedTargetPath, file, None), HttpStatus.SC_OK)

    val (status, body) = api.getStaging(stagingUuid)
    assertEquals(status, HttpStatus.SC_OK)
    val uploadedFile = findFileInStaging(body, s"folder/$TestFile")
    assertTrue(uploadedFile.isDefined, "Uploaded file should be present")
    assertEquals(uploadedFile.get.get("size").asLong(), file.length())
  }

  @Test(description = "Upload and automatically unzip a package")
  def uploadAndUnzipTest(): Unit = withStaging { stagingUuid =>
    assertEquals(
      api.uploadFile(stagingUuid, "package.zip", getTestFile("package.zip"), Some("unzipped")),
      HttpStatus.SC_OK
    )

    val (status, body) = api.getStaging(stagingUuid)
    assertEquals(status, HttpStatus.SC_OK)
    assertTrue(findFileInStaging(body, "unzipped/ConditionsOfUse.html").isDefined)
  }

  @Test(description = "Delete a specific file from the staging area")
  def deleteFileTest(): Unit = withStaging { stagingUuid =>
    assertEquals(
      api.uploadFile(stagingUuid, "test.txt", getTestFile("avatar.png"), None),
      HttpStatus.SC_OK
    )
    assertEquals(api.deleteFile(stagingUuid, "test.txt"), HttpStatus.SC_NO_CONTENT)

    val (status, body) = api.getStaging(stagingUuid)
    assertEquals(status, HttpStatus.SC_OK)
    assertEquals(body.get("files").size(), 0)
  }

  @Test(description = "Check file metadata using HEAD request")
  def headFileTest(): Unit = withStaging { stagingUuid =>
    val file = getTestFile("avatar.png")
    assertEquals(api.uploadFile(stagingUuid, "avatar.png", file, None), HttpStatus.SC_OK)

    val (status, contentLength) = api.headFile(stagingUuid, "avatar.png")
    assertEquals(status, HttpStatus.SC_OK)
    assertEquals(contentLength.toLong, file.length())
  }

  @Test(description = "Upload a file in multiple parts and stitch them together")
  def multipartUploadTest(): Unit = withStaging { stagingUuid =>
    val filename = "multipart-dummy-file.txt"

    val (startStatus, uploadId) = api.startMultipart(stagingUuid)
    assertEquals(startStatus, HttpStatus.SC_CREATED)

    val (p1Status, part1) = api.uploadPart(stagingUuid, uploadId, 1, "First half of the file. ")
    assertEquals(p1Status, HttpStatus.SC_OK)

    val (p2Status, part2) = api.uploadPart(stagingUuid, uploadId, 2, "Second half of the file.")
    assertEquals(p2Status, HttpStatus.SC_OK)

    val (compStatus, compLoc) = api.completeMultipart(stagingUuid, filename, uploadId, part1, part2)
    assertEquals(compStatus, HttpStatus.SC_OK)
    assertTrue(compLoc.exists(_.endsWith(filename)), s"Location header should point to '$filename'")

    val (getStatus, body) = api.getStaging(stagingUuid)
    assertEquals(getStatus, HttpStatus.SC_OK)
    assertTrue(
      findFileInStaging(body, filename).isDefined,
      "Multipart-uploaded file should be present"
    )
  }

  private def findFileInStaging(stagingNode: JsonNode, exactFilePath: String): Option[JsonNode] =
    Option(stagingNode).flatMap(n => Option(n.get("files"))).filter(_.isArray).flatMap {
      filesNode =>
        filesNode.elements().asScala.find(_.get("name").asText() == exactFilePath)
    }

  private def getTestFile(attachmentName: String): File =
    new File(AbstractPage.getPathFromUrl(Attachments.get(attachmentName)))

  private class StagingApi {
    private val endpoint = getTestConfig.getInstitutionUrl + "api/staging/"

    private def execute[T](method: HttpMethod)(onSuccess: HttpMethod => T): (Int, T) = {
      val status = makeClientRequest(method)
      val result = if (status >= 200 && status < 300) onSuccess(method) else null.asInstanceOf[T]
      (status, result)
    }

    private def getHeader(m: HttpMethod, name: String): String =
      Option(m.getResponseHeader(name)).map(_.getValue).orNull

    def createStaging(): (Int, String) =
      execute(new PostMethod(endpoint))(m => getHeader(m, "x-eps-stagingid"))

    def getStaging(stagingUuid: String): (Int, JsonNode) =
      execute(new GetMethod(endpoint + stagingUuid))(m =>
        mapper.readTree(m.getResponseBodyAsStream)
      )

    def uploadFile(
        stagingUuid: String,
        targetPath: String,
        file: File,
        unzipTo: Option[String]
    ): Int = {
      val method = new PutMethod(s"$endpoint$stagingUuid/$targetPath")
      method.setRequestEntity(new FileRequestEntity(file, "application/octet-stream"))
      unzipTo.foreach(u => method.setQueryString(Array(new NameValuePair("unzipto", u))))
      makeClientRequest(method)
    }

    def deleteFile(stagingUuid: String, filePath: String): Int =
      makeClientRequest(new DeleteMethod(s"$endpoint$stagingUuid/$filePath"))

    def deleteStaging(stagingUuid: String): Int =
      makeClientRequest(new DeleteMethod(endpoint + stagingUuid))

    def headFile(stagingUuid: String, filePath: String): (Int, String) =
      execute(new HeadMethod(s"$endpoint$stagingUuid/$filePath"))(m =>
        getHeader(m, "Content-Length")
      )

    def startMultipart(stagingUuid: String): (Int, String) =
      execute(new PostMethod(s"$endpoint$stagingUuid/multipart")) { m =>
        mapper.readTree(m.getResponseBodyAsStream).get("uploadId").asText()
      }

    def uploadPart(
        stagingUuid: String,
        uploadId: String,
        partNumber: Int,
        content: String
    ): (Int, UploadedPart) = {
      val method = new PutMethod(s"$endpoint$stagingUuid/multipart/$uploadId/$partNumber")
      method.setRequestEntity(new StringRequestEntity(content, "text/plain", "UTF-8"))

      val status = makeClientRequest(method)
      val part   =
        if (status == HttpStatus.SC_OK) UploadedPart(partNumber, getHeader(method, "ETag"))
        else null
      (status, part)
    }

    def completeMultipart(
        stagingUuid: String,
        targetPath: String,
        uploadId: String,
        parts: UploadedPart*
    ): (Int, Option[String]) = {
      val method = new PostMethod(s"$endpoint$stagingUuid/$targetPath/complete")
      method.setQueryString(Array(new NameValuePair("uploadId", uploadId)))

      val payload   = mapper.createObjectNode()
      val partArray = payload.putArray("parts")
      parts.foreach(p =>
        partArray.addObject().put("partNumber", p.partNumber).put("etag", p.etag.replace("\"", ""))
      )

      method.setRequestEntity(
        new StringRequestEntity(payload.toString, "application/json", "UTF-8")
      )
      val status = makeClientRequest(method)
      (status, Option(getHeader(method, "Location")))
    }
  }
}
