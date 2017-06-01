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

package com.dytech.edge.admin.wizard.editor;

import java.util.UUID;

import javax.swing.JTabbedPane;

import com.dytech.edge.admin.wizard.editor.drm.DRMAccessControlTab;
import com.dytech.edge.admin.wizard.editor.drm.DRMConfigTab;
import com.dytech.edge.admin.wizard.editor.drm.DRMRequireAcceptanceFromTab;
import com.dytech.edge.admin.wizard.editor.drm.DRMRightsTab;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.admin.wizard.model.DrmPageModel;
import com.dytech.edge.wizard.beans.DRMPage;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteUserService;

public class DrmPageEditor extends Editor
{
	private static final long serialVersionUID = 1L;

	private DRMConfigTab configTab;
	private DRMRightsTab rightsTab;
	private DRMAccessControlTab accessControlTab;
	private DRMRequireAcceptanceFromTab requireAcceptanceFromTab;

	public DrmPageEditor(Control control, int wizardType, SchemaModel schema)
	{
		super(control, wizardType, schema);

		// we need to set up a UUID if one hasn't already been set
		DrmPageModel ctl = (DrmPageModel) getControl();
		if( ctl.getPage().getUuid() == null )
		{
			ctl.getPage().setUuid(UUID.randomUUID().toString());
		}
	}

	@Override
	@SuppressWarnings("nls")
	public void init()
	{
		setShowScripting(true);

		configTab = new DRMConfigTab();
		rightsTab = new DRMRightsTab();
		accessControlTab = new DRMAccessControlTab(getClientService().getService(RemoteUserService.class));
		requireAcceptanceFromTab = new DRMRequireAcceptanceFromTab();

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drmpageeditor.config"), configTab);
		tabs.addTab(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drmpageeditor.rights"), rightsTab);
		tabs.addTab(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drmpageeditor.access"), accessControlTab);
		tabs.addTab(CurrentLocale.get("com.dytech.edge.admin.wizard.editor.drmpageeditor.requires"),
			requireAcceptanceFromTab);

		addSection(tabs);
	}

	@Override
	protected void loadControl()
	{
		DrmPageModel control = (DrmPageModel) getControl();

		DRMPage page = control.getPage();
		configTab.load(page);
		rightsTab.load(page);
		accessControlTab.load(page);
		requireAcceptanceFromTab.load(page);
	}

	@Override
	protected void saveControl()
	{
		DrmPageModel control = (DrmPageModel) getControl();

		DRMPage page = new DRMPage();
		page.setUuid(control.getPage().getUuid());
		page.setScript(control.getPage().getScript());
		control.setWrappedObject(page);
		configTab.save(page);
		rightsTab.save(page);
		accessControlTab.save(page);
		requireAcceptanceFromTab.save(page);
	}
}
