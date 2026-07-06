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

import io.circe.generic.auto._
import org.slf4j.{Logger, LoggerFactory}
import sttp.client3.circe.asJson
import sttp.client3.{asByteArray, asString, basicRequest}
import sttp.model.Uri

/** A single entry in a staging area listing. Mirrors the JSON of
  * `com.tle.web.api.interfaces.beans.BlobBean`, redefined here to avoid a dependency on the core
  * module; only the fields the Admin Console needs are modelled.
  *
  * @param name
  *   the entry's path relative to the listing root — the staging area root, or the `path` the
  *   listing was scoped to
  * @param size
  *   the file size in bytes (zero for folder entries)
  * @param folder
  *   `Some(true)` for a folder entry; only present when folder entries were requested
  */
final case class StagingBlob(name: String, size: Long, folder: Option[Boolean] = None)

/** A staging area listing. Mirrors the JSON of
  * `com.tle.web.api.staging.interfaces.beans.StagingBean`.
  *
  * @param uuid
  *   the UUID of the staging area
  * @param files
  *   the flat list of entries in the staging area
  */
final case class StagingListing(uuid: String, files: List[StagingBlob])

/** The `StagingApi` object provides a client for the /api/staging endpoints of the openEQUELLA REST
  * API. These operate on the staging area attached to an entity edit/create session, identified by
  * the staging UUID found in the session's `EntityPack`.
  */
object StagingApi {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(StagingApi.getClass)
  private val API_PATH                = "staging"

  /** Builds the URI for a file within a staging area. The file path is split into segments so that
    * each is individually encoded — passing the whole path as one segment would encode its `/`
    * separators.
    */
  /** Builds the base URI for a staging area. */
  private def stagingUri(stagingUuid: String)(implicit cfg: RestConfiguration): Uri =
    cfg.apiUrl().addPath(API_PATH, stagingUuid)

  private def fileUri(stagingUuid: String, filepath: String)(implicit
      cfg: RestConfiguration
  ): Uri =
    stagingUri(stagingUuid).addPath(splitPath(filepath))

  /** The query params accepted by the staging listing endpoint, typed at the call site and
    * serialised to query-string values here.
    */
  private def listingParams(path: String, folders: Boolean, checksums: Boolean) =
    Map("path" -> path, "folders" -> folders.toString, "checksums" -> checksums.toString)

  /** Retrieves the listing of a staging area as a flat list of entries.
    *
    * @param stagingUuid
    *   the UUID of the staging area to list
    * @param path
    *   when given, scopes the listing to this folder: entry names are relative to it, folder
    *   entries (including empty folders) are included, and per-file checksums are skipped for a
    *   faster listing. When `None`, lists the whole staging area with the server's default
    *   behaviour (files only).
    */
  def getStaging(stagingUuid: String, path: Option[String] = None)(implicit
      cfg: RestConfiguration
  ): Either[RestError, StagingListing] = {
    val baseUri = stagingUri(stagingUuid)
    val uri     = path.fold(baseUri) { p =>
      baseUri.addParams(listingParams(p, folders = true, checksums = false))
    }
    val request = basicRequest.get(uri).response(asJson[StagingListing])
    handleDecodedResult(request)(_.getMessage)
  }

  /** Uploads a file to a staging area, overwriting any existing file at the same path.
    *
    * @param stagingUuid
    *   the UUID of the staging area to upload to
    * @param filepath
    *   the path (relative to the staging area root) to write the file to
    * @param bytes
    *   the contents of the file
    */
  def putFile(stagingUuid: String, filepath: String, bytes: Array[Byte])(implicit
      cfg: RestConfiguration
  ): Either[RestError, Unit] = {
    val request = basicRequest
      .put(fileUri(stagingUuid, filepath))
      .body(bytes)
      .response(asString)
    handleResult(extractAction(request), send(request)) { _ =>
      Right(())
    }
  }

  /** Downloads a file from a staging area.
    *
    * @param stagingUuid
    *   the UUID of the staging area to download from
    * @param filepath
    *   the path (relative to the staging area root) of the file to download
    */
  def getFile(stagingUuid: String, filepath: String)(implicit
      cfg: RestConfiguration
  ): Either[RestError, Array[Byte]] = {
    val request = basicRequest
      .get(fileUri(stagingUuid, filepath))
      .response(asByteArray)
    handleDecodedResult(request)(identity)
  }

  /** Deletes a file or folder (including its contents) from a staging area.
    *
    * @param stagingUuid
    *   the UUID of the staging area to delete from
    * @param filepath
    *   the path (relative to the staging area root) of the file or folder to delete
    */
  def deleteFile(stagingUuid: String, filepath: String)(implicit
      cfg: RestConfiguration
  ): Either[RestError, Unit] = {
    val request = basicRequest
      .delete(fileUri(stagingUuid, filepath))
      .response(asString)
    handleResult(extractAction(request), send(request)) { _ =>
      Right(())
    }
  }
}
