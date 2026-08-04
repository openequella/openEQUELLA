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

import java.util
import scala.jdk.CollectionConverters._

/** Rebuilds the hierarchical `FileEntry` tree expected by the Admin Console (e.g.
  * `EntityStagingFileViewer`) from the flat listing returned by the REST staging API. Expects a
  * scoped listing (see `StagingApi.getStaging` with a path): entry names are relative to the
  * listing root and folder entries are present, so empty folders are represented.
  */
object StagingFileTree {

  /** A listing entry positioned relative to the folder currently being built.
    *
    * @param path
    *   the entry's remaining path, as segments relative to the current folder
    * @param size
    *   the file size in bytes (zero for folders)
    * @param isFolder
    *   whether the entry is a folder
    */
  private final case class ListedEntry(path: List[String], size: Long, isFolder: Boolean) {

    /** Is this entry a direct child of the current folder (rather than inside a subfolder)? */
    def isDirectChild: Boolean = path.size == 1

    /** The entry's name at the current level — its own name for a direct child, otherwise the
      * subfolder it sits beneath.
      */
    def topName: String = path.head

    /** The same entry, repositioned relative to the subfolder [[topName]]. */
    def descend: ListedEntry = copy(path = path.tail)
  }

  /** Builds a `FileEntry` tree from a flat staging listing.
    *
    * @param blobs
    *   the flat listing, with names relative to the listing root; entries flagged with
    *   `folder = Some(true)` become folder nodes (in addition to any folders implied by file paths)
    * @param rootName
    *   the name for the returned root folder entry. The viewer includes this name in the paths it
    *   builds for subsequent service calls, so it must be the last segment of the folder the
    *   listing was scoped to (e.g. `"displaytemplate"`).
    * @return
    *   a folder `FileEntry` containing the tree of all listed entries
    */
  def buildFileEntryTree(blobs: List[StagingBlob], rootName: String): FileEntry = {
    val entries = blobs
      .map(blob => ListedEntry(splitPath(blob.name), blob.size, blob.folder.contains(true)))
      .filter(_.path.nonEmpty)

    buildFolder(rootName, entries)
  }

  /** Recursively builds a folder entry: its subfolders first, then its own files, each sorted by
    * name.
    *
    * Not tail recursive (a parent entry needs its children built first), which is safe here: the
    * recursion depth is the folder nesting depth, which the filesystem's path-length limits keep
    * far below any stack concern.
    */
  private def buildFolder(name: String, entries: List[ListedEntry]): FileEntry = {
    val (direct, nested) = entries.partition(_.isDirectChild)
    makeFolder(name, subfolderEntries(direct, nested) ++ fileEntries(direct))
  }

  /** Builds a folder's subfolder entries: one per direct folder entry or subfolder implied by a
    * deeper path (merged, so neither is duplicated), each built from the entries beneath it.
    */
  private def subfolderEntries(
      direct: List[ListedEntry],
      nested: List[ListedEntry]
  ): List[FileEntry] = {
    val names = (direct.filter(_.isFolder) ++ nested).map(_.topName).distinct.sorted
    names.map(name => buildFolder(name, nested.filter(_.topName == name).map(_.descend)))
  }

  /** Builds a folder's own file entries, sorted by name. */
  private def fileEntries(direct: List[ListedEntry]): List[FileEntry] =
    direct
      .filterNot(_.isFolder)
      .sortBy(_.topName)
      .map(entry => makeFile(entry.topName, entry.size))

  private def makeFolder(name: String, children: List[FileEntry]): FileEntry = {
    val folder = new FileEntry(true)
    folder.setName(name)
    folder.setFiles(new util.ArrayList[FileEntry](children.asJava))
    folder
  }

  private def makeFile(name: String, size: Long): FileEntry = {
    val file = new FileEntry(false)
    file.setName(name)
    file.setLength(size)
    file
  }
}
