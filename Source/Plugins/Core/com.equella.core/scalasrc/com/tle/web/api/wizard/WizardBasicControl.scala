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
import com.dytech.edge.wizard.beans.control.{WizardControl, WizardControlItem}
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.tle.common.i18n.{LangUtils}
import com.tle.web.api.language.LanguageStringHelper.getStringFromCurrentLocale
import scala.collection.JavaConverters._

/** Data structure for Wizard Control option
  *
  * @param text Text of the option. None in some components like Calendar.
  * @param value  Value of the option.
  */
case class WizardControlOption(text: Option[String], value: String)

object WizardControlOption {
  def apply(item: WizardControlItem): WizardControlOption = {
    WizardControlOption(
      text = Option(item.getName).map(name => LangUtils.getString(name)),
      value = item.getValue,
    )
  }
}

// This trait is used to support the union type of different Wizard control types.
// We can probably remove it trait once we can use Scala 3.
sealed trait WizardControlDefinition

/**
  * Data structure for key information of 'com.dytech.edge.wizard.beans.control.WizardControl'.
  *
  * @param mandatory Whether the control must have a value.
  * @param reload Whether to reload all controls after this control has different selection. This may affect how scripting works.
  * @param include #todo
  * @param size1 #todo
  * @param size2 #todo
  * @param customName The controls' customised name.
  * @param title Title of the control.
  * @param description Description of the control.
  * @param visibilityScript Script running on Client to control the visibility of the control.
  * @param targetNodes Schema nodes that the control targets to.
  * @param options Options available for selection.
  * @param powerSearchFriendlyName #todo
  * @param controlType Type of the control.
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
    powerSearchFriendlyName: Option[String],
    controlType: String
) extends WizardControlDefinition

object WizardBasicControl {
  def apply(control: WizardControl): WizardBasicControl = {
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
      options = control.getItems.asScala.map(i => WizardControlOption(i)).toList,
      powerSearchFriendlyName = getStringFromCurrentLocale(control.getPowerSearchFriendlyName),
      controlType = control.getClassType
    )
  }
}

/**
  * Data structure for Calendar control.
  *
  * @param basicControl The basic control providing common fields.
  * @param isRange Whether to support a date range.
  */
case class WizardCalendarControl(@JsonUnwrapped
                                 basicControl: WizardBasicControl,
                                 isRange: Boolean)
    extends WizardControlDefinition

/**
  * Data structure for ShuffleList control.
  *
  * @param basicControl The basic control providing common fields.
  * @param isTokenise Whether to tokenise the value.
  * @param isForceUnique Whether each value must be unique.
  * @param isCheckDuplication Whether to check duplicated values.
  */
case class WizardShuffleListControl(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    isTokenise: Boolean,
    isForceUnique: Boolean,
    isCheckDuplication: Boolean
) extends WizardControlDefinition

/**
  * Data structure for EditBox control.
  *
  * @param basicControl The basic control providing common fields.
  * @param isAllowLinks Whether to allow links.
  * @param isNumber Whether to use numbers only.
  * @param isAllowMultiLang Whether to allow multiple lines.
  * @param isForceUnique Whether each value must be unique.
  * @param isCheckDuplication Whether to check duplicated values.
  */
case class WizardEditBoxControl(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    isAllowLinks: Boolean,
    isNumber: Boolean,
    isAllowMultiLang: Boolean,
    isForceUnique: Boolean,
    isCheckDuplication: Boolean,
) extends WizardControlDefinition

/**
  * Data structure for Custom Wizard control.
  *
  * @param basicControl The basic control providing common fields.
  * @param attributes Custom attributes
  */
case class WizardCustomControl(
    @JsonUnwrapped
    basicControl: WizardBasicControl,
    attributes: Map[Object, Object]
) extends WizardControlDefinition

/**
  * Data structure for unknown Wizard control.
  */
case class UnknownWizardControl() extends WizardControlDefinition {
  val controlType: String = "unknown"
}
