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

package io.github.openequella.graphql.test

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.util.zip.ZipInputStream

/** Utility functions for working with ZIP streams in tests. */
object ZipTestHelper {

  /** Wraps a byte array in a [[ZipInputStream]] for inspection. */
  def bytesToZipInputStream(bytes: Array[Byte]): ZipInputStream =
    new ZipInputStream(new ByteArrayInputStream(bytes))

  /** Finds and reads the `_entity.xml` entry from a ZIP stream, returning [[None]] if absent. */
  def extractEntityXml(zis: ZipInputStream): Option[String] =
    Iterator
      .continually(zis.getNextEntry)
      .takeWhile(_ != null)
      .find(_.getName == "_entity.xml")
      .map(_ => readXmlFile(zis))

  private def readXmlFile(zis: ZipInputStream): String = {
    val reader = new BufferedReader(new InputStreamReader(zis, "UTF-8"))
    Iterator
      .continually(reader.readLine())
      .takeWhile(_ != null)
      .mkString("\n")
  }
}
