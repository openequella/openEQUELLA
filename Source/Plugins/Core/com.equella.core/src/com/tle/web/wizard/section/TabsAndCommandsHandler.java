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

package com.tle.web.wizard.section;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import java.util.ArrayList;
import java.util.List;

public class TabsAndCommandsHandler extends CollectInterfaceHandler<SectionTabable> {
  private static final String COMMANDS_KEY = "$COMMANDABLES$"; // $NON-NLS-1$

  public TabsAndCommandsHandler() {
    super(SectionTabable.class);
  }

  @Override
  public void registered(String id, SectionTree tree, Section section) {
    super.registered(id, tree, section);
    List<SectionCommandable> commandables = tree.getAttribute(COMMANDS_KEY);
    if (commandables == null) {
      commandables = new ArrayList<SectionCommandable>();
      tree.setAttribute(COMMANDS_KEY, commandables);
    }

    if (section instanceof SectionCommandable) {
      commandables.add((SectionCommandable) section);
    }
  }

  public List<SectionCommandable> getCommandables(SectionInfo info) {
    return info.getTreeAttribute(COMMANDS_KEY);
  }
}
