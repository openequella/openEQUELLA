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

package com.tle.admin.controls

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}
import java.nio.charset.StandardCharsets
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}
import scala.util.Using

/** Tests for the `.wzc` wizard control exchange format: a zip archive holding the control's XML
  * definition in a single UTF-8 `_control.xml` entry, byte-compatible with the format produced by
  * the legacy server-side implementation.
  */
class ExportedControlZipSpec extends AnyFunSpec with Matchers {

  private val ControlXml =
    "<com.tle.admin.controls.ExportedControl><version>test</version></com.tle.admin.controls.ExportedControl>"

  private def zipEntries(bytes: Array[Byte]): Map[String, String] =
    Using.resource(new ZipInputStream(new ByteArrayInputStream(bytes))) { zis =>
      Iterator
        .continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .map(entry => entry.getName -> new String(zis.readAllBytes(), StandardCharsets.UTF_8))
        .toMap
    }

  private def zipOf(entries: (String, String)*): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    Using.resource(new ZipOutputStream(out, StandardCharsets.UTF_8)) { zos =>
      entries.foreach { case (name, content) =>
        zos.putNextEntry(new ZipEntry(name))
        zos.write(content.getBytes(StandardCharsets.UTF_8))
        zos.closeEntry()
      }
    }
    out.toByteArray
  }

  describe("zip") {
    it("produces a zip file whose sole _control.xml entry holds exactly the input XML") {
      zipEntries(ExportedControlZip.zip(ControlXml)) shouldBe Map("_control.xml" -> ControlXml)
    }
  }

  describe("unzip") {
    it("extracts the XML from a zip produced by zip, byte-for-byte") {
      ExportedControlZip.unzip(ExportedControlZip.zip(ControlXml)) shouldBe ControlXml
    }

    it("round-trips non-ASCII content") {
      val unicodeXml = "<control><title>Тестовый контроль — 日本語</title></control>"
      ExportedControlZip.unzip(ExportedControlZip.zip(unicodeXml)) shouldBe unicodeXml
    }

    it("reads a zip built externally with a _control.xml entry (legacy format)") {
      ExportedControlZip.unzip(zipOf("_control.xml" -> ControlXml)) shouldBe ControlXml
    }

    it("throws an IOException for data that is not a zip file") {
      an[IOException] should be thrownBy {
        ExportedControlZip.unzip("this is not a zip file".getBytes(StandardCharsets.UTF_8))
      }
    }

    it("throws an IOException when the zip has no _control.xml entry") {
      an[IOException] should be thrownBy {
        ExportedControlZip.unzip(zipOf("something-else.xml" -> ControlXml))
      }
    }
  }
}
