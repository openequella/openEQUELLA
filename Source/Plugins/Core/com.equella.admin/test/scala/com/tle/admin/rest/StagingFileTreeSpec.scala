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

package com.tle.admin.rest

import com.tle.common.filesystem.FileEntry
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters._

/** Tests for the rebuilding of a hierarchical `FileEntry` tree from the flat, scoped listing
  * returned by the REST staging API (entry names relative to the listing root, folder entries
  * included).
  */
class StagingFileTreeSpec extends AnyFunSpec with Matchers {

  private val RootName = "displaytemplate"

  private def file(path: String, size: Long = 1L): StagingBlob = StagingBlob(path, size)

  private def folder(path: String): StagingBlob = StagingBlob(path, 0L, folder = Some(true))

  private def childNames(entry: FileEntry): List[String] =
    entry.getFiles.asScala.map(_.getName).toList

  private def child(entry: FileEntry, name: String): FileEntry =
    entry.getFiles.asScala
      .find(_.getName == name)
      .getOrElse(fail(s"Expected child '$name' in folder '${entry.getName}'"))

  describe("buildFileEntryTree") {
    it("builds a flat folder of files") {
      val blobs = List(file("style.css", 120L), file("logo.png", 4096L))

      val root = StagingFileTree.buildFileEntryTree(blobs, RootName)

      root.isFolder shouldBe true
      root.getName shouldBe RootName
      childNames(root) shouldBe List("logo.png", "style.css")
      child(root, "logo.png").isFolder shouldBe false
      child(root, "logo.png").getLength shouldBe 4096L
    }

    it("creates intermediate folders implied by nested file paths") {
      val blobs = List(
        file("images/icons/star.png"),
        file("images/banner.jpg"),
        file("template.ftl")
      )

      val root = StagingFileTree.buildFileEntryTree(blobs, RootName)

      // Folders are listed before files
      childNames(root) shouldBe List("images", "template.ftl")

      val images = child(root, "images")
      images.isFolder shouldBe true
      childNames(images) shouldBe List("icons", "banner.jpg")

      val icons = child(images, "icons")
      icons.isFolder shouldBe true
      childNames(icons) shouldBe List("star.png")
    }

    it("renders explicit folder entries, including empty folders") {
      val blobs = List(folder("images"), folder("images/empty"), file("images/banner.jpg"))

      val root = StagingFileTree.buildFileEntryTree(blobs, RootName)

      val images = child(root, "images")
      images.isFolder shouldBe true
      childNames(images) shouldBe List("empty", "banner.jpg")

      val empty = child(images, "empty")
      empty.isFolder shouldBe true
      empty.getFiles.asScala shouldBe Symbol("empty")
    }

    it("does not duplicate a folder that is both explicit and implied by a file path") {
      val blobs = List(folder("images"), file("images/banner.jpg"))

      val root = StagingFileTree.buildFileEntryTree(blobs, RootName)

      childNames(root) shouldBe List("images")
      childNames(child(root, "images")) shouldBe List("banner.jpg")
    }

    it("returns an empty root folder for an empty listing") {
      val root = StagingFileTree.buildFileEntryTree(List.empty, RootName)

      root.isFolder shouldBe true
      root.getName shouldBe RootName
      root.getFiles.asScala shouldBe Symbol("empty")
    }
  }
}
