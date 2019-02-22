/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.harvester.tool;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.common.EntityPack;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.security.PrivilegeTree.Node;
import java.util.ArrayList;
import java.util.List;

public class HarvesterProfileEditor extends BaseEntityEditor<HarvesterProfile> {

  private final HarvesterProfileTool tool2;
  private HarvesterDetailsTab harvestDetailsTab;
  private HarvesterActionsTab harvestActionsTab;

  public HarvesterProfileEditor(HarvesterProfileTool tool, boolean readonly) {
    super(tool, readonly);
    tool2 = tool;
  }

  @Override
  public void load(EntityPack<HarvesterProfile> bentity, boolean isLoaded) {
    setType(bentity.getEntity().getType());
    super.load(bentity, isLoaded);
  }

  public void setType(String type) {
    harvestDetailsTab = new HarvesterDetailsTab();
    harvestActionsTab = new HarvesterActionsTab(this);

    harvestDetailsTab.setPlugin(tool2.getToolInstance(type));
    harvestActionsTab.setPlugin(tool2.getToolInstance(type));
  }

  @Override
  protected AbstractDetailsTab<HarvesterProfile> constructDetailsTab() {
    return harvestDetailsTab;
  }

  @Override
  protected String getEntityName() {
    return getString("harvester.entityname"); // $NON-NLS-1$
  }

  @Override
  protected String getWindowTitle() {
    return getString("harvester.windowtitle"); // $NON-NLS-1$
  }

  @Override
  public String getDocumentName() {
    return getString("harvester.name"); // $NON-NLS-1$
  }

  @Override
  protected List<? extends BaseEntityTab<HarvesterProfile>> getTabs() {
    List<BaseEntityTab<HarvesterProfile>> list = new ArrayList<BaseEntityTab<HarvesterProfile>>();
    list.add(harvestDetailsTab);
    list.add(harvestActionsTab);
    list.add(new AccessControlTab<HarvesterProfile>(Node.HARVESTER_PROFILE));
    return list;
  }
}
