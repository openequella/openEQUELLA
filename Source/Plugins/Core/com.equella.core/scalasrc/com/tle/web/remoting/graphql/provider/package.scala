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

package com.tle.web.remoting.graphql

import com.tle.common.EntityPack
import com.tle.common.beans.exception.NotFoundException
import com.tle.web.remoting.graphql.schema.types.EditableEntity

import java.util.Base64

package object provider {

  /** A utility method to wrap a function that may throw a `NotFoundException` and return an
    * `Option`. If the function throws a `NotFoundException`, it returns `None`, otherwise it
    * returns `Some(value)`.
    *
    * @param fn
    *   the function to execute
    * @tparam T
    *   the type of the value returned by the function
    * @return
    *   an `Option[T]` that is `None` if the function throws a `NotFoundException`, otherwise `Some`
    *   with the result of the function
    */
  def noneIfNotFound[T](fn: => T): Option[T] = {
    try {
      Option(fn)
    } catch {
      case _: NotFoundException => None
    }
  }

  /** Validates, decodes, and imports a Base64-encoded entity zip file, returning an
    * [[EditableEntity]] ready for the edit lifecycle. This utility centralises the common pattern
    * of validating and decoding Base64 zip input before calling an entity import service, and is
    * intended to be reused across all entity provider import methods.
    *
    * Follows the same pattern as `ProviderError.Try` for consistent error handling.
    *
    * Example usage:
    * {{{
    *   importBaseEntity("metadata schema", zipBase64, schemaService.importEntity)(MetadataSchema.apply)
    * }}}
    *
    * @param entityName
    *   a human-readable name for the entity type, used in error messages and logging.
    * @param zipBase64
    *   the Base64-encoded zip file content to import.
    * @param importFn
    *   the service function to call with the decoded bytes, returning an `EntityPack`.
    * @param convertFn
    *   a function to convert the raw entity from the pack into the desired GraphQL type `T`.
    * @tparam E
    *   the Java entity type returned inside the `EntityPack`.
    * @tparam T
    *   the target GraphQL type to convert the entity into.
    * @return
    *   a `Right` containing the `EditableEntity[T]` on success, or a `Left` with a `ProviderError`
    *   on failure (including empty input, invalid Base64, or service errors).
    */
  def importBaseEntity[E <: com.tle.beans.entity.BaseEntity, T](
      entityName: String,
      zipBase64: String,
      importFn: Array[Byte] => EntityPack[E]
  )(convertFn: E => T): Either[ProviderError, EditableEntity[T]] =
    Either
      .cond(
        zipBase64.trim.nonEmpty,
        zipBase64,
        ProviderError("Import failed: empty zip data provided", ErrorCode.BAD_REQUEST)
      )
      .flatMap(zip =>
        ProviderError.Try(s"Failed to import $entityName: ") {
          EditableEntity(importFn(Base64.getDecoder.decode(zip)), convertFn)
        }
      )
}
