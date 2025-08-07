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

package io.github.openequella.graphql.api

import io.github.openequella.graphql.api.views.BaseEntityReferenceView
import io.github.openequella.graphql.client.Queries
import io.github.openequella.graphql.{Client, ClientConfiguration}

import java.util.Base64

/** Provides access to the openEQUELLA metadata schema API.
  */
object MetadataSchemaApi {

  /** Lists all metadata schemas available in the system.
    *
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of BaseEntityReferenceView representing the metadata schemas or a list of
    *   ApiError if the operation failed. If no schemas are found, an empty list is returned.
    */
  def listSchemas()(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], List[BaseEntityReferenceView]] = {
    val query = Queries.metadataSchemas {
      BaseEntityReferenceView.selector
    }

    Client.query(query)
  }

  /** Retrieves the ID of a metadata schema by its UUID.
    *
    * @param uuid
    *   The UUID of the metadata schema.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of ApiError or an Option containing the ID of the metadata schema if found,
    *   None if not found.
    */
  def getIdByUuid(uuid: String)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Long]] = {
    val query = Queries.metadataSchemaIdForUuid(uuid)

    Client.query(query)
  }

  /** Exports a metadata schema as a ZIP file.
    *
    * @param id
    *   The ID of the metadata schema to export.
    * @param withSecurity
    *   Whether to include security information in the export.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an Array[Byte] containing the exported schema as a zip file.
    */
  def exportSchema(id: Long, withSecurity: Boolean)(implicit
      cfg: ClientConfiguration
  ): Either[List[ApiError], Option[Array[Byte]]] = {
    val query = Queries.metadataSchemaExport(id, withSecurity)

    // The query returns a base64 encoded string (representing a zip file), which we need to decode
    // into an Array[Byte]. Returning Array[Byte] removes the need for the client to be aware
    // of the base64 encoding and decoding process.
    Client.query(query).map(_.map(base64ToBytes))
  }

  private def base64ToBytes(base64Zip: String): Array[Byte] =
    Base64.getDecoder.decode(base64Zip)
}
