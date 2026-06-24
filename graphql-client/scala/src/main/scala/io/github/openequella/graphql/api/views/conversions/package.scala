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

package io.github.openequella.graphql.api.views

/** Provides conversions between View types (returned by GraphQL queries) and Input types (required
  * by GraphQL mutations).
  *
  * This package contains:
  *   - [[CollectionConversions]] - Transformers and conversion functions specific to collection
  *     definitions
  *   - [[CommonConversions]] - Transformers for shared entity types (LanguageBundle, EntityDetails,
  *     etc.)
  *   - [[MetadataSchemaConversions]] - Transformers and conversion functions specific to metadata
  *     schemas
  *   - [[withConvertedInput]] - Generic helper for converting a view to input and executing a block
  *
  * Usage example:
  * {{{
  * import io.github.openequella.graphql.api.views.conversions.{MetadataSchemaConversions, withConvertedInput}
  *
  * val result = withConvertedInput(editView, MetadataSchemaConversions.toInput) { input =>
  *   flatMutate(MetadataSchemaMutations.stopEdit(input, unlock = true) { MetadataSchemaView.selector })
  * }
  * }}}
  */
package object conversions {
  import cats.implicits._
  import io.github.openequella.graphql.api.{ApiError, UnknownError}

  /** Converts a view to its GraphQL input type and passes it to an execution block.
    *
    * Any non-fatal exception thrown by `convert` is caught and returned as an UnknownError, so
    * callers get a consistent Left rather than an unchecked exception.
    *
    * @param view
    *   The view object to convert.
    * @param convert
    *   Function that converts the view to the corresponding GraphQL input type.
    * @param execute
    *   Function that uses the converted input to execute a mutation and return the result.
    * @return
    *   Either a list of ApiError or the result of `execute`.
    */
  def withConvertedInput[View, Input, A](
      view: View,
      convert: View => Input
  )(execute: Input => Either[List[ApiError], A]): Either[List[ApiError], A] =
    Either
      .catchNonFatal(convert(view))
      .leftMap(e =>
        List(
          UnknownError(
            s"Failed to convert ${view.getClass.getSimpleName} to input type: ${e.getMessage}"
          )
        )
      )
      .flatMap(execute)
}
