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

package com.tle.web.settings.section;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.*;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.*;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.settings.EditableSettings;
import com.tle.web.settings.SettingsList;
import com.tle.web.template.Decorations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("nls")
public class SettingsSection extends AbstractPrototypeSection<Object> implements ViewableChildInterface, HtmlRenderer
{
	@PlugKey("title")
	private static Label TITLE_LABEL;

	@PlugKey("settings.filter.all")
	private static String KEY_ALL;
	@PlugKey("settings.categories")
	private static Label LABEL_TABLE_HEADER;

	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Component(name = "st")
	private Table settingsTable;

	// FIXME all comment out codes are left as what they are, this because the
	// issue #7089 implies that the codes maybe used again in the future.

	// @Component(name = "fb")
	// private SingleSelectionList<Void> filterButtons;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		settingsTable.setColumnHeadings(LABEL_TABLE_HEADER);
		settingsTable.setColumnSorts(Sort.PRIMARY_ASC);
		settingsTable.setWrap(true);
		settingsTable.setFilterable(true); // Show/hide filter box

		// JSCallable filterFunction = settingsTable.createFilterFunction();

		// filterButtons.setEventHandler(JSHandler.EVENT_CHANGE,
		// new OverrideHandler(filterFunction,
		// filterButtons.createGetExpression()));

		// filterButtons.setListModel(listModel);
		// filterButtons.setAlwaysSelect(true);
		// filterButtons.setDefaultRenderer("buttongroup");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Decorations decorations = Decorations.getDecorations(context);
		decorations.setTitle(TITLE_LABEL);
		decorations.setContentBodyClass("settings");

		buildTable(context);

		// Hide if "all" is the only option
		// if( filterButtons.getListModel().getOptions(context).size() == 1 )
		// {
		// filterButtons.setDisplayed(context, false);
		// }

		return viewFactory.createResult("settings.ftl", this);
	}

	private void buildTable(RenderEventContext context)
	{
		// Print all viewable sections and add groups as filterData
		List<EditableSettings> settingsList = SettingsList.editableSettingsJ();

		// Build table from row data
		for( final EditableSettings rData : settingsList )
		{
			final CombinedRenderer renderable = new CombinedRenderer(new LinkRenderer(SettingsList.asLinkOrNull(rData)),
				new BrRenderer(), new LabelRenderer(new TextLabel(rData.description())));
			final TableRow row = settingsTable.addRow(context, renderable);

			final String filterData = rData.description();
			if( !Check.isEmpty(filterData) )
			{
				row.setFilterData(filterData);
			}
		}

		settingsTable.makePresentation(context);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return SectionUtils.canViewChildren(info, this);
	}

	public Table getSettingsTable()
	{
		return settingsTable;
	}

}
