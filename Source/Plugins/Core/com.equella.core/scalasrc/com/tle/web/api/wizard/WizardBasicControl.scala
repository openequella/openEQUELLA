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

package com.tle.web.api.wizard

import com.dytech.edge.wizard.TargetNode
import com.dytech.edge.wizard.beans.control.{Calendar, EditBox, WizardControl, WizardControlItem}
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.tle.common.i18n.LangUtils
import com.tle.common.taxonomy.SelectionRestriction
import com.tle.common.taxonomy.wizard.TermSelectorControl.TermStorageFormat
import com.tle.web.api.language.LanguageStringHelper.getStringFromCurrentLocale
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import scala.jdk.CollectionConverters._
import scala.util.Try

/** Data structure representing an option provided by a 'Option' type control such as CheckBox Group
  * and Shuffle List.
  *
  * @param text
  *   Text of the option. None in some components like Calendar.
  * @param value
  *   Value of the option.
  */
case class WizardControlOption(text: Option[String], value: String)

object WizardControlOption {
  def apply(item: WizardControlItem): WizardControlOption = {
    WizardControlOption(
      text = Option(item.getName).map(name => LangUtils.getString(name)),
      value = item.getValue
    )
  }
}

/** Common properties for controlling unique value. Typically used in text editing type controls
  * like EditBox.
  *
  * @param isForceUnique
  *   Whether each value must be unique.
  * @param isCheckDuplication
  *   Whether to check duplicated values.
  */
case class ControlUniqueConstraints(isForceUnique: Boolean, isCheckDuplication: Boolean)

// This trait is used to support the union type of different Wizard control types.
// We can probably remove it once we can use Scala 3.
sealed trait WizardControlDefinition

/** Data structure for key information of 'com.dytech.edge.wizard.beans.control.WizardControl'.
  *
  * @param mandatory
  *   Whether the control must have a value.
  * @param reload
  *   Whether to reload all controls after this control has different selection. This may affect how
  *   scripting works.
  * @param include
  *   Whether the control is selectable in Admin Console Advanced Search editor.
  * @param size1
  *   Number of columns typically used in Radio Button groups and Checkbox groups.
  * @param size2
  *   Number of rows typically used in EditBox.
  * @param customName
  *   The controls' customised name which is used in the Admin Console.
  * @param title
  *   Title of the control.
  * @param description
  *   Description of the control.
  * @param visibilityScript
  *   Script which controls the visibility of the control. (Commonly run when rendering control.)
  * @param targetNodes
  *   Schema nodes that the control targets to.
  * @param options
  *   Options available for selection.
  * @param powerSearchFriendlyName
  *   Text displayed in the criteria summary instead of the Schema node.
  * @param controlType
  *   Type of the control.
  */
case class WizardBasicControl(
    mandatory: Boolean,
    reload: Boolean,
    include: Boolean,
    size1: Int,
    size2: Int,
    customName: Option[String],
    title: Option[String],
    description: Option[String],
    visibilityScript: Option[String],
    targetNodes: List[TargetNode],
    options: List[WizardControlOption],
    defaultValues: List[String],
    powerSearchFriendlyName: Option[String],
    controlType: String
) extends WizardControlDefinition

object WizardBasicControl {
  def getDefaultValues(options: List[WizardControlItem], controlType: String): List[String] = {
    def processValue(item: WizardControlItem): String = {
      val value = item.getValue
      controlType match {
        case Calendar.CLASS =>
          // If the value is numeric, convert it to Int and plus it to today.
          Try(value.toInt).toOption
            .map(i => LocalDate.now.plus(i, ChronoUnit.DAYS).toString)
            .getOrElse(value)
        case _ => value
      }
    }

    // Option of controls whose types are in the below list is always the default option.
    // For those not in the list, check `isDefaultOption` of the option.
    options
      .filter(o => List(EditBox.CLASS, Calendar.CLASS).contains(controlType) || o.isDefaultOption)
      .map(processValue)
  }

  def apply(control: WizardControl): WizardBasicControl = {
    val options = control.getItems.asScala.toList
    WizardBasicControl(
      mandatory = control.isMandatory,
      reload = control.isReload,
      include = control.isInclude,
      size1 = control.getSize1,
      size2 = control.getSize2,
      customName = Option(control.getCustomName),
      title = getStringFromCurrentLocale(control.getTitle),
      description = getStringFromCurrentLocale(control.getDescription),
      visibilityScript = Option(control.getScript),
      targetNodes = control.getTargetnodes.asScala.toList,
      options = options.map(o => WizardControlOption(o)),
      defaultValues = getDefaultValues(options, control.getClassType),
      powerSearchFriendlyName = getStringFromCurrentLocale(control.getPowerSearchFriendlyName),
      controlType = control.getClassType
    )
  }
}

/** Data structure for Calendar control.
  *
  * @param basicControl
  *   The basic control providing common fields.
  * @param isRange
  *   Whether to support a date range. If false, should only display a single calendar control and
  *   supply a single date value. However if true, two controls should be displayed with the values
  *   from both representing a range.
  */
case class WizardCalendarControl(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    isRange: Boolean,
    dateFormat: String
) extends WizardControlDefinition

/** Data structure for ShuffleList control.
  *
  * @param basicControl
  *   The basic control providing common fields.
  * @param isTokenise
  *   Whether to tokenise the value. If true, an '*' must be appended to the schema node in the
  *   Lucene query.
  */
case class WizardShuffleListControl(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    @JsonUnwrapped
    uniqueConstraints: ControlUniqueConstraints,
    isTokenise: Boolean
) extends WizardControlDefinition

/** Data structure for EditBox control.
  *
  * @param basicControl
  *   The basic control providing common fields.
  * @param isAllowLinks
  *   Whether to allow links.
  * @param isNumber
  *   Whether to use numbers only.
  * @param isAllowMultiLang
  *   Whether to support multiple languages.
  */
case class WizardEditBoxControl(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    isAllowLinks: Boolean,
    isNumber: Boolean,
    isAllowMultiLang: Boolean,
    @JsonUnwrapped
    uniqueConstraints: ControlUniqueConstraints
) extends WizardControlDefinition

/** Data structure for Custom Wizard control such as Term Selector and Owner Selector.
  *
  * @param basicControl
  *   The basic control providing common fields.
  * @param attributes
  *   A map where keys and values represent configurations that are specific to a certain type of
  *   CustomControl.
  */
case class WizardCustomControl(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    attributes: Map[String, Object]
) extends WizardControlDefinition

/** Data structure for TermSelector control.
  *
  * @param basicControl
  *   The basic control providing common fields.
  * @param selectedTaxonomy
  *   Taxonomy selected to search terms.
  * @param isAllowMultiple
  *   Whether to allow multiple selections.
  * @param isAllowAddTerms
  *   Whether to allow adding terms.
  * @param termStorageFormat
  *   Whether to use full taxonomy path or only the selected term.
  * @param selectionRestriction
  *   The restriction of term selection.
  * @param displayType
  *   Which UI to be displayed - TermSelector has three different UI implementations.
  */
case class WizardTermSelector(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    displayType: String,
    isAllowAddTerms: Boolean,
    isAllowMultiple: Boolean,
    selectedTaxonomy: String,
    selectionRestriction: SelectionRestriction,
    termStorageFormat: TermStorageFormat
) extends WizardControlDefinition

/** Data structure for UserSelector control.
  *
  * @param basicControl
  *   The basic control providing common fields.
  * @param isSelectMultiple
  *   Whether to allow selecting multiple users.
  * @param isRestricted
  *   Whether to restrict user selection by groups.
  * @param restrictedTo
  *   Groups which the selection is limited to.
  */
case class WizardUserSelector(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    isSelectMultiple: Boolean,
    isRestricted: Boolean,
    restrictedTo: java.util.Set[String]
) extends WizardControlDefinition

/** Data structure for unknown Wizard control.
  */
case class UnknownWizardControl() extends WizardControlDefinition {
  val controlType: String = "unknown"
}
