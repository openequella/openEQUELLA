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

import com.dytech.edge.wizard.beans.control.Calendar.DateFormat
import com.dytech.edge.wizard.beans.control.{
  Calendar,
  CheckBoxGroup,
  CustomControl,
  EditBox,
  Html,
  ListBox,
  RadioGroup,
  ShuffleBox,
  ShuffleList,
  WizardControl
}
import com.tle.common.taxonomy.wizard.TermSelectorControl
import com.tle.common.wizard.controls.userselector.UserSelectorControl
import org.slf4j.LoggerFactory

object WizardControlHelper {
  private val LOGGER = LoggerFactory.getLogger(getClass)

  /** `CustomControl` has multiple subclasses. Call this function to convert a subclass to
    * `WizardControlDefinition`. If fail to convert, return `UnknownWizardControl`.
    *
    * @param customControl
    *   An instance of CustomControl.
    */
  def wizardCustomControlConverter(customControl: CustomControl): WizardControlDefinition = {
    customControl.getClassType match {
      case UserSelectorControl.CLASS_TYPE =>
        val control = new UserSelectorControl(customControl)
        WizardUserSelector(
          WizardBasicControl(control),
          control.isSelectMultiple,
          control.isRestricted(UserSelectorControl.KEY_RESTRICT_USER_GROUPS),
          control.getRestrictedTo(UserSelectorControl.KEY_RESTRICT_USER_GROUPS)
        )
      case TermSelectorControl.CLASS_TYPE =>
        val control = new TermSelectorControl(customControl)
        WizardTermSelector(
          WizardBasicControl(control),
          control.getDisplayType,
          control.isAllowAddTerms,
          control.isAllowMultiple,
          control.getSelectedTaxonomy,
          control.getSelectionRestriction,
          control.getTermStorageFormat
        )
      case _ =>
        LOGGER.error("Unknown Custom Control type")
        UnknownWizardControl()
    }
  }

  /** Convert a list of `com.dytech.edge.wizard.beans.control.WizardControl` to a list of
    * `WizardControlDefinition`
    *
    * @param controls
    *   The list of Java Wizard controls.
    */
  def wizardControlConverter(controls: List[WizardControl]): List[WizardControlDefinition] = {
    controls.map {
      // These controls do not have special fields
      case c @ (_: ListBox | _: CheckBoxGroup | _: RadioGroup | _: ShuffleBox | _: Html) =>
        WizardBasicControl(c)
      case c: Calendar =>
        WizardCalendarControl(
          WizardBasicControl(c),
          c.isRange,
          Option(c.getFormat).getOrElse(DateFormat.DMY).name()
        )
      case c: ShuffleList =>
        WizardShuffleListControl(
          WizardBasicControl(c),
          ControlUniqueConstraints(c.isForceUnique, c.isCheckDuplication),
          c.isTokenise
        )
      case c: EditBox =>
        WizardEditBoxControl(
          WizardBasicControl(c),
          c.isAllowLinks,
          c.isNumber,
          c.isAllowMultiLang,
          ControlUniqueConstraints(c.isForceUnique, c.isCheckDuplication)
        )
      case c: CustomControl => wizardCustomControlConverter(c)
      case _ =>
        LOGGER.error("Unknown Wizard Control type")
        UnknownWizardControl()
    }
  }
}
