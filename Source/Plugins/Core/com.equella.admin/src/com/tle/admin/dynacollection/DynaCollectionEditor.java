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

package com.tle.admin.dynacollection;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.DynaCollection;
import com.tle.common.applet.client.EntityCache;
import com.tle.common.security.PrivilegeTree.Node;
import java.util.ArrayList;
import java.util.List;

public class DynaCollectionEditor extends BaseEntityEditor<DynaCollection> {
  public DynaCollectionEditor(BaseEntityTool<DynaCollection> tool, boolean readonly) {
    super(tool, readonly);
  }

  @Override
  protected AbstractDetailsTab<DynaCollection> constructDetailsTab() {
    return new DetailsTab();
  }

  @Override
  protected List<BaseEntityTab<DynaCollection>> getTabs() {
    EntityCache cache = new EntityCache(clientService);

    List<BaseEntityTab<DynaCollection>> tabs = new ArrayList<BaseEntityTab<DynaCollection>>();
    tabs.add((DetailsTab) detailsTab);
    tabs.add(new FilterTab(cache));
    tabs.add(new VirtualisationTab());
    tabs.add(new AccessControlTab<DynaCollection>(Node.DYNA_COLLECTION));
    return tabs;
  }

  @Override
  protected String getEntityName() {
    return getString("entityname"); // $NON-NLS-1$
  }

  @Override
  protected String getWindowTitle() {
    return getString("windowtitle"); // $NON-NLS-1$
  }

  @Override
  public String getDocumentName() {
    return getString("entityname"); // $NON-NLS-1$
  }
}
