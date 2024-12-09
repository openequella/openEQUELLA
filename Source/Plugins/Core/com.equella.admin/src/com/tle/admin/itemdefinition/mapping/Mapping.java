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

package com.tle.admin.itemdefinition.mapping;

import com.tle.beans.entity.itemdef.MetadataMapping;
import javax.swing.JComponent;

public interface Mapping {
  /** Gets the viewable component for this mapping. */
  JComponent getComponent();

  /** Retrieve the XML for the current mapping. */
  void save(MetadataMapping mapping);

  /** Load the mapping editor from the given XML. */
  void loadItem(MetadataMapping mapping);
}
