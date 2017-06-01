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

package com.tle.admin.collection.summarydisplay;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.tle.admin.Driver;
import com.tle.admin.baseentity.EditorState;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;

@SuppressWarnings("nls")
public class NoConfigurationConfig extends JPanel implements SummaryDisplayConfig
{
	@Override
	public void setup()
	{
		JLabel label = new JLabel(CurrentLocale.get("com.tle.admin.collection.tool.summarysections.noconfiguration"));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		setLayout(new GridLayout(1, 1));
		add(label);
	}

	@Override
	public void clearChanges()
	{
		// Nothing to do
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return false;
	}

	@Override
	public void save(SummarySectionsConfig element)
	{
		// Nothing to do
	}

	@Override
	public void load(SummarySectionsConfig element)
	{
		// Nothing to do
	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	public void setDriver(Driver driver)
	{
		// Nothing to do
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		// Nothing to do
	}

	@Override
	public void setSchemaModel(SchemaModel model)
	{
		// Nothing to do
	}

	@Override
	public void setClientService(ClientService service)
	{
		// Nothing to do
	}
}