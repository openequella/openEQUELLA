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

package com.tle.web.contentrestrictions;

import com.google.inject.name.Names;
import com.tle.web.sections.equella.guice.SectionsModule;

public class ContentRestrictionsModule extends SectionsModule {

  @Override
  protected void configure() {
    bind(Object.class)
        .annotatedWith(Names.named("/access/contentrestrictions"))
        .toProvider(contentRestrictionsTree());
  }

  private NodeProvider contentRestrictionsTree() {
    NodeProvider node = node(RootContentRestrictionsSection.class);
    node.innerChild(EditContentRestrictionsSection.class);
    return node;
  }
}
