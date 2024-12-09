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

package com.tle.web.api.search.model

import com.dytech.edge.queries.FreeTextQuery
import com.tle.common.i18n.CurrentTimeZone
import com.tle.common.util.{Dates, LocalDate}
import com.tle.core.freetext.queries.{FreeTextBooleanQuery, FreeTextDateQuery, FreeTextFieldQuery}

import scala.util.{Failure, Success, Try}

/** Provides one Wizard control's targeted schema nodes and supplied values.
  *
  * @param schemaNodes
  *   The 'fullPath's for the targetNode.
  * @param values
  *   Values of one Wizard Control.
  * @param queryType
  *   The query type which must be either 'DateRange', 'Tokenised' or 'Phrase'.
  */
case class WizardControlFieldValue(
    schemaNodes: Array[String],
    values: Array[String],
    queryType: String
)

/** Provides a list of `WizardControlFieldValue` to help build the full criteria.
  *
  * @param advancedSearchCriteria
  *   A list of `WizardControlFieldValue`.
  */
case class AdvancedSearchParameters(advancedSearchCriteria: Array[WizardControlFieldValue])

object AdvancedSearchParameters {
  // Build FreeTextFieldQuery for each value and put all values into one FreeTextBooleanQuery.
  // The relationship between each FreeTextFieldQuery is `OR`.
  private def buildTextFieldQuery(
      field: String,
      values: Array[String],
      isTokenised: Boolean = false
  ): FreeTextQuery = {
    val queries: Array[FreeTextQuery] =
      values.map(v => {
        val q = new FreeTextFieldQuery(field, v, false)
        q.setTokenise(isTokenised)
        q
      })

    new FreeTextBooleanQuery(false, false, queries: _*)
  }

  // Build FreeTextDateQuery. Basic data structure validation for Date range is included.
  private def buildDateRangeQuery(field: String, values: Array[String]): FreeTextDateQuery = {
    // Function to parse the date string when it's neither null or empty.
    def getLocalDate(date: String): Option[LocalDate] = {
      Option(date).collect {
        case d if d.nonEmpty =>
          Try {
            new LocalDate(d, Dates.ISO_DATE_ONLY, CurrentTimeZone.get)
          } match {
            case Success(value) => value
            case Failure(e) =>
              throw new IllegalArgumentException(
                s"Failed to build date range query for field $field due to ${e.getMessage}"
              )
          }
      }
    }

    def buildFreeTextDateQuery(start: Option[LocalDate], end: Option[LocalDate]) =
      new FreeTextDateQuery(
        field,
        start.orNull,
        end.orNull,
        true,
        true
      )

    values match {
      // When the array has two values, the first value is start and the second one is end.
      case Array(start, end) =>
        buildFreeTextDateQuery(getLocalDate(start), getLocalDate(end))
      // When the array has only one value, the value must be start.
      case Array(start) =>
        buildFreeTextDateQuery(getLocalDate(start), None)
      // For others like an empty array or an array having more than 2 values, throw an exception.
      case _ =>
        throw new IllegalArgumentException(
          "Wrong data structure for building a date range query - must have one or two values"
        )
    }
  }

  // Build FreeTextQuery for one control, depending on the the query type.
  // If the control has multiple schema nodes, build a FreeTextQuery for each node
  // and join all into one FreeTextBooleanQuery.
  // The relationship between each FreeTextQuery is `OR`.
  private def buildCriteriaForOneControl(fieldValue: WizardControlFieldValue): FreeTextQuery = {
    val WizardControlFieldValue(schemaNodes, values, queryType) = fieldValue

    val queries = schemaNodes.map(node =>
      queryType match {
        case "DateRange" =>
          buildDateRangeQuery(node, values)
        case "Phrase" =>
          buildTextFieldQuery(node, values)
        case "Tokenised" =>
          buildTextFieldQuery(node, values, isTokenised = true)
      }
    )

    new FreeTextBooleanQuery(false, false, queries: _*)
  }

  /** Function to build a FreeTextQuery for each Wizard Control and put all the FreeTextQuery into
    * one FreeTextBooleanQuery. The relationship between each FreeTextQuery is `AND`.
    *
    * @param fieldValues
    *   A array of `WizardControlFieldValue` providing each Wizard controls' information
    */
  def buildAdvancedSearchCriteria(
      fieldValues: Array[WizardControlFieldValue]
  ): FreeTextBooleanQuery = {
    val queries = fieldValues.map(buildCriteriaForOneControl)
    new FreeTextBooleanQuery(false, true, queries: _*)
  }
}
