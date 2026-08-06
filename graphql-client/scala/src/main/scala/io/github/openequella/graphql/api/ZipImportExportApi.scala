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

import caliban.client.SelectionBuilder
import io.github.openequella.graphql.ClientConfiguration

import java.util.Base64

/** Mixin for APIs that send and receive entity data as base64-encoded ZIP files over GraphQL.
  *
  * The GraphQL transport represents ZIP file data as base64 strings. This trait encapsulates that
  * encoding/decoding concern so that API methods can work directly with `Array[Byte]` without
  * needing to know about the underlying base64 representation.
  *
  * @tparam Q
  *   The GraphQL query type.
  * @tparam M
  *   The GraphQL mutation type.
  */
trait ZipImportExportApi[Q, M] extends NestedApi[Q, M] {

  /** Executes an export query and decodes the base64-encoded ZIP result to bytes.
    *
    * @param exportQuery
    *   A selection that returns an optional base64 string representing a ZIP file.
    * @param cfg
    *   The client configuration.
    * @return
    *   Either a list of errors or an optional `Array[Byte]` containing the ZIP file contents.
    */
  protected def exportZip(
      exportQuery: SelectionBuilder[Q, Option[String]]
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], Option[Array[Byte]]] =
    query(exportQuery).map(_.map(Base64.getDecoder.decode))

  /** Encodes a ZIP file as base64, builds the import mutation, and executes it.
    *
    * @param zip
    *   The ZIP file bytes to import.
    * @param buildImportMutation
    *   A function from the base64-encoded string to the import mutation selection.
    * @param cfg
    *   The client configuration.
    * @tparam A
    *   The type returned by the import mutation.
    * @return
    *   Either a list of errors or the result of the import mutation.
    */
  protected def importZip[A](zip: Array[Byte])(
      buildImportMutation: String => SelectionBuilder[M, Option[A]]
  )(implicit cfg: ClientConfiguration): Either[List[ApiError], A] =
    flatMutate(buildImportMutation(Base64.getEncoder.encodeToString(zip)))
}
