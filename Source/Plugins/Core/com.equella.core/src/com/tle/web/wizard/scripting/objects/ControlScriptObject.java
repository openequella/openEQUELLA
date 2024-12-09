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

package com.tle.web.wizard.scripting.objects;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'ctrl' variable in script
 *
 * @author aholland
 */
public interface ControlScriptObject extends ScriptObject {
  /**
   * Gets the current value of this control.
   *
   * @return The stored value of this control
   */
  String getValue();

  /**
   * Sets a new value for this control. Will update any metadata that this control is mapped to.
   *
   * @param value The new value for this control.
   */
  void setValue(String value);

  /**
   * @return true if this control is not to be shown on the page.
   */
  boolean isHidden();

  /**
   * If set to true, the control is not shown on the page, but will retain it's value.
   *
   * @param hidden true to hide the control
   */
  void setHidden(boolean hidden);

  /**
   * @return false if this control is not to be shown on the page and not to store any metadata
   */
  boolean isVisible();

  /**
   * If set to false, the control is not shown on the page AND <em>it's value will be cleared</em>
   * Be careful not to confuse with setHidden, which does not clear the value.
   *
   * @param visible The visibility of this control.
   */
  void setVisible(boolean visible);

  /**
   * Determines if this control has a value (for mandatory purposes)
   *
   * @return true if the control has no value
   */
  boolean isEmpty();

  /** Clear the invalid state of the control. Same as ctrl.setInvalid(false, null); */
  void clearInvalid();

  /**
   * Mark the control as invalid so that the wizard is not considered complete. (The item will not
   * be allowed to be published until the control is valid)
   *
   * @param invalid true if the control is not valid.
   * @param message A message to display to the user.
   */
  void setInvalid(boolean invalid, String message);

  /**
   * Returns the ID of this control when displayed in the HTML<br>
   * E.g. &lt;input id="theid" type="text"&gt;<br>
   *
   * <p>Note that not all controls will be rendered as a simple HTML equivalent (for example: the
   * load attachments control) in this case the form ID is actually an ID prefix for all of the HTML
   * elements rendered by this control.
   *
   * @return The ID or ID-prefix as rendered in the HTML
   */
  String getFormId();

  /**
   * Returns the parent of the calling ControlScriptObject. A parent will exist generally in the
   * case when a control is nested in a group / repeater.
   *
   * @return The ControlScriptObject parent, null if not available.
   */
  ControlScriptObject getParent();

  /**
   * Returns the index of the ControlScriptObject parameter, assuming there is a direct parent-child
   * relationship with the ControlScriptObject making the call, and the parent is a list of
   * ControlScriptObjects, such as a repeater.
   *
   * @param child The child script object to retrieve the index for.
   * @return The index of the ControlScriptObject parameter, relative to it's parent. -1 if not
   *     available.
   */
  int getIndex(ControlScriptObject child);
}
