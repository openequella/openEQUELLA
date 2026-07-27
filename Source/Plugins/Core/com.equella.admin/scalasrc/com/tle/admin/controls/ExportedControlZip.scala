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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}
import java.nio.charset.StandardCharsets
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}
import scala.util.Using

/** Reads and writes the wizard control exchange file format (`.wzc`): a zip archive holding the
  * control's XML definition in a single `_control.xml` entry (UTF-8 encoded).
  *
  * The format matches what the legacy server-side implementation
  * (`ItemDefinitionServiceImpl.exportControl` / `importControl`) produced and consumed, so files
  * exported by older versions remain importable and vice versa. Historically the zipping and
  * unzipping were performed server-side via a remoting round trip, even though no institution data
  * or server state was involved — it is a purely local transformation, so it now lives in the Admin
  * Console.
  */
object ExportedControlZip {
  private val ControlXmlEntry = "_control.xml"

  /** Packages a wizard control's XML definition as the contents of a `.wzc` zip file.
    *
    * @param controlXml
    *   the XML definition of the control (treated as opaque).
    * @return
    *   the bytes of a zip file containing the XML as its sole `_control.xml` entry.
    * @see
    *   [[unzip]] for the inverse operation.
    */
  def zip(controlXml: String): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    Using.resource(new ZipOutputStream(out, StandardCharsets.UTF_8)) { zos =>
      zos.putNextEntry(new ZipEntry(ControlXmlEntry))
      zos.write(controlXml.getBytes(StandardCharsets.UTF_8))
      zos.closeEntry()
    }
    out.toByteArray
  }

  /** Extracts a wizard control's XML definition from the contents of a `.wzc` zip file.
    *
    * @param zipFileData
    *   the bytes of a zip file containing a `_control.xml` entry.
    * @return
    *   the XML definition stored in the zip.
    * @throws IOException
    *   if the data is not a zip file, or contains no `_control.xml` entry.
    * @see
    *   [[zip]] for the inverse operation.
    */
  def unzip(zipFileData: Array[Byte]): String =
    Using.resource(new ZipInputStream(new ByteArrayInputStream(zipFileData))) { zis =>
      Iterator
        .continually(zis.getNextEntry)
        .takeWhile(_ != null)
        .find(_.getName == ControlXmlEntry)
        .map(_ => new String(zis.readAllBytes(), StandardCharsets.UTF_8))
        .getOrElse(
          throw new IOException(s"No $ControlXmlEntry entry found in the supplied zip file")
        )
    }
}
