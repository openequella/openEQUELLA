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

package com.tle.admin.reporting;

import com.tle.admin.baseentity.AccessControlTab;
import com.tle.admin.baseentity.BaseEntityEditor;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.tools.common.BaseEntityTool;
import com.tle.beans.entity.report.Report;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.plugins.AbstractPluginService;
import java.util.ArrayList;
import java.util.List;

public class ReportEditor extends BaseEntityEditor<Report> {
  private static String KEYPFX = AbstractPluginService.getMyPluginId(ReportEditor.class) + ".";

  /** Constructs a new SchemaManager. */
  public ReportEditor(BaseEntityTool<Report> tool, boolean readonly) {
    super(tool, readonly);
  }

  @Override
  protected String getEntityName() {
    return "report"; //$NON-NLS-1$
  }

  @Override
  public String getDocumentName() {
    return CurrentLocale.get(KEYPFX + "reporteditor.docname"); // $NON-NLS-1$
  }

  @Override
  protected String getWindowTitle() {
    return CurrentLocale.get(KEYPFX + "reporteditor.title"); // $NON-NLS-1$
  }

  @Override
  protected List<BaseEntityTab<Report>> getTabs() {
    List<BaseEntityTab<Report>> tabs = new ArrayList<BaseEntityTab<Report>>();
    tabs.add((DetailsTab) detailsTab);
    tabs.add(new AccessControlTab<Report>(Node.REPORT));
    return tabs;
  }

  @Override
  protected AbstractDetailsTab<Report> constructDetailsTab() {
    return new DetailsTab();
  }
}
