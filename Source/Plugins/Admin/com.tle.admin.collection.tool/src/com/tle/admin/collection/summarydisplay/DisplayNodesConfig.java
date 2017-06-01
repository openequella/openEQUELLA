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
import java.util.List;

import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ChangeDetector;
import com.thoughtworks.xstream.XStream;
import com.tle.admin.baseentity.EditorState;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.itemdefinition.DisplayNodeList;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.DisplayNode;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.applet.client.ClientService;
import com.tle.i18n.BundleCache;

public class DisplayNodesConfig extends AbstractOnlyTitleConfig
{
	private DisplayNodeList displayNodeList;
	private ChangeDetector changeDetector;
	private SchemaModel schemaModel;
	private XStream xstream;

	public DisplayNodesConfig()
	{
		xstream = new XStream();
		xstream.setClassLoader(getClass().getClassLoader());
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	@Override
	public void setup()
	{
		displayNodeList = new DisplayNodeList(schemaModel, true);

		final JLabel titleLabel = new JLabel(getTitleLabelKey());
		title = new I18nTextField(BundleCache.getLanguages());

		setLayout(new MigLayout());
		add(titleLabel, "gap 50, split 2");
		add(title, "grow, wrap");
		add(displayNodeList);

		changeDetector = new ChangeDetector();
		changeDetector.watch(displayNodeList);
		changeDetector.watch(title);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(SummarySectionsConfig element)
	{
		title.load(element.getBundleTitle());
		if( element.getConfiguration() != null )
		{
			Object fromXML = xstream.fromXML(element.getConfiguration());
			displayNodeList.load((List<DisplayNode>) fromXML);
		}
	}

	@Override
	public void save(SummarySectionsConfig element)
	{
		String toXML = xstream.toXML(displayNodeList.save());
		element.setConfiguration(toXML);
		LanguageBundle titleBundle = title.save();
		element.setBundleTitle(titleBundle);
	}

	@Override
	public void setSchemaModel(SchemaModel model)
	{
		this.schemaModel = model;
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		// Ignore
	}

	@Override
	public void setClientService(ClientService service)
	{
		// Ignore
	}

	@Override
	public Component getComponent()
	{
		return this;
	}
}
