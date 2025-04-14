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

package com.tle.common.scripting.types;

import java.io.Serializable;
import java.util.List;

/** NavigationNode in script */
public interface NavigationNodeScriptType extends Serializable {
  /**
   * @return The display name of the node
   */
  String getDescription();

  /**
   * @param description The display name of the node
   */
  void setDescription(String description);

  /**
   * @return An unmodifiable list. Use addTab(String, AttachmentScriptType,
   *     NavigationNodeScriptType) if you want to add a tab to this list.
   */
  List<NavigationTabScriptType> getTabs();
}
