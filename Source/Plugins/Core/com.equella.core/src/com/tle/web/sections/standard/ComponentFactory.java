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

package com.tle.web.sections.standard;

import com.tle.web.sections.SectionTree;

/**
 * A factory class for create components.
 *
 * @author jmaginnis
 */
public interface ComponentFactory {
  Button createButton(String parentId, String id, SectionTree tree);

  Link createLink(String parentId, String id, SectionTree tree);

  SingleSelectionList<?> createSingleSelectionList(String parentId, String id, SectionTree tree);

  MultiSelectionList<?> createMultiSelectionList(String parentId, String id, SectionTree tree);

  TextField createTextField(String parentId, String id, SectionTree tree);

  Checkbox createCheckbox(String parentId, String id, SectionTree tree);

  /**
   * Assigns a unique ID to the component and calls tree.registerInnerSection
   *
   * @param parentId
   * @param id
   * @param tree
   * @param component
   */
  void registerComponent(
      String parentId, String id, SectionTree tree, AbstractHtmlComponent<?> component);

  void setupComponent(
      String parentId, String id, SectionTree tree, AbstractHtmlComponent<?> component);

  <T extends AbstractHtmlComponent<?>> T createComponent(
      String parentId, String id, SectionTree tree, Class<T> clazz, boolean register);
}
