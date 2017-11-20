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

package com.tle.admin.controls.universal;

import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.JLabel;

import com.tle.core.plugins.AbstractPluginService;
import net.miginfocom.swing.MigLayout;

import com.tle.admin.gui.common.DynamicChoicePanel;
import com.tle.admin.schema.SchemaModel;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.universal.UniversalControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

/**
 * @author Aaron
 */
public abstract class UniversalControlSettingPanel extends DynamicChoicePanel<UniversalSettings>
{
	private int wizardType;
	private SchemaModel schemaModel;
	private UniversalControl control;
	private ClientService clientService;

	private String KEY_PFX = AbstractPluginService.getMyPluginId(getClass()) + ".";

	protected String getString(String key)
	{
		return CurrentLocale.get(getKey(key));
	}

	protected String getKey(String key)
	{
		return KEY_PFX+key;
	}

	/**
	 * Uses a MigLayout("wrap 2, insets 10 15 20 5", "[][fill, grow]")
	 */
	protected UniversalControlSettingPanel()
	{
		super(new MigLayout("wrap 2, insets 10 15 20 5", "[][fill, grow]"));
		JLabel label = new JLabel(CurrentLocale.get(getTitleKey()));
		Font titleFont = label.getFont();
		label.setFont(new Font(titleFont.getName(), Font.BOLD, titleFont.getSize()));
		add(label, "span 2, gapbottom 10");
	}

	protected UniversalControlSettingPanel(LayoutManager layout)
	{
		super(layout);
	}

	protected abstract String getTitleKey();

	public void init(UniversalControl control, int wizardType, SchemaModel schemaModel, ClientService clientService)
	{
		this.control = control;
		this.wizardType = wizardType;
		this.schemaModel = schemaModel;
		this.clientService = clientService;
	}

	public int getWizardType()
	{
		return wizardType;
	}

	public SchemaModel getSchemaModel()
	{
		return schemaModel;
	}

	public UniversalControl getControl()
	{
		return control;
	}

	public ClientService getClientService()
	{
		return clientService;
	}
}
