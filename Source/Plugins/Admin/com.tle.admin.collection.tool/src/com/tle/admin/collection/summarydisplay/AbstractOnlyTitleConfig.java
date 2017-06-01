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

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.gui.ChangeDetector;
import com.tle.admin.baseentity.EditorState;
import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.i18n.BundleCache;

public abstract class AbstractOnlyTitleConfig extends JPanel implements SummaryDisplayConfig
{
	protected I18nTextField title;
	protected ChangeDetector changeDetector;

	@SuppressWarnings("nls")
	@Override
	public void setup()
	{
		final JLabel titleLabel;
		String titleLabelKey = getTitleLabelKey();
		titleLabel = new JLabel(titleLabelKey);

		title = new I18nTextField(BundleCache.getLanguages());

		add(titleLabel, "split 2");
		add(title, "grow, pushx, wrap");
		if( showTitleHelp() )
		{
			JLabel titleHelpLabel = new JLabel(
				CurrentLocale
					.get("com.tle.admin.collection.tool.summarysections.abstractonlytitle.bundletitle.help.label"));
			add(titleHelpLabel, "gapleft 25, wrap");
		}

		changeDetector = new ChangeDetector();
		changeDetector.watch(title);
	}

	public boolean showTitleHelp()
	{
		return false;
	}

	public String getTitleLabelKey()
	{
		return CurrentLocale.get("com.tle.admin.collection.tool.summarysections.abstractonlytitle.bundletitle.label");
	}

	@Override
	public void load(SummarySectionsConfig sectionElement)
	{
		title.load(sectionElement.getBundleTitle());
	}

	@Override
	public void save(SummarySectionsConfig sectionElement)
	{
		LanguageBundle titleBundle = title.save();
		sectionElement.setBundleTitle(titleBundle);
	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public void setClientService(ClientService service)
	{
		// Nothing to do here
	}

	@Override
	public void setState(EditorState<ItemDefinition> state)
	{
		// Nothing to do here
	}

	@Override
	public void setSchemaModel(SchemaModel model)
	{
		// Nothing to do here
	}
}
