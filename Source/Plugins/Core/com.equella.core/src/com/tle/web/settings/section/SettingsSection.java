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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.BrRenderer;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.settings.AbstractParentSettingsSection;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class SettingsSection extends AbstractPrototypeSection<Object> implements ViewableChildInterface, HtmlRenderer
{
	@PlugKey("title")
	private static Label TITLE_LABEL;

	@PlugKey("settings.filter.all")
	private static String KEY_ALL;
	@PlugKey("settings.categories")
	private static Label LABEL_TABLE_HEADER;

	private PluginTracker<AbstractParentSettingsSection<?>> settingsExtensions;
	private PluginTracker<Object> settingsGroupExtensions;

	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Component(name = "st")
	private Table settingsTable;

	// FIXME all comment out codes are left as what they are, this because the
	// issue #7089 implies that the codes maybe used again in the future.

	// @Component(name = "fb")
	// private SingleSelectionList<Void> filterButtons;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		settingsExtensions = new PluginTracker<AbstractParentSettingsSection<?>>(pluginService, "com.tle.web.settings",
			"settingsExtension", null);
		settingsExtensions.setBeanKey("class");
		settingsGroupExtensions = new PluginTracker<Object>(pluginService, "com.tle.web.settings", "settingsGroupingExtension",
			PluginTracker.LOCAL_ID_FOR_KEY);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		for( AbstractParentSettingsSection<?> ext : settingsExtensions.getBeanList() )
		{
			tree.registerInnerSection(ext, id);
		}

		settingsTable.setColumnHeadings(LABEL_TABLE_HEADER);
		settingsTable.setColumnSorts(Sort.PRIMARY_ASC);
		settingsTable.setWrap(true);
		settingsTable.setFilterable(true); // Show/hide filter box

		// JSCallable filterFunction = settingsTable.createFilterFunction();

		// filterButtons.setEventHandler(JSHandler.EVENT_CHANGE,
		// new OverrideHandler(filterFunction,
		// filterButtons.createGetExpression()));

		DynamicHtmlListModel<Void> listModel = new DynamicHtmlListModel<Void>()
		{
			@Override
			protected Option<Void> getTopOption()
			{
				return new VoidKeyOption(KEY_ALL, "");
			}

			@Override
			protected Iterable<Option<Void>> populateOptions(SectionInfo info)
			{
				List<Extension> groupExtensions = Lists.newArrayList();
				Map<String, List<AbstractParentSettingsSection<?>>> groupingMap = getGroupingMap(info);

				// Make a list of groups which have visible sections
				for( Extension ext : settingsGroupExtensions.getExtensions() )
				{
					if( !Check.isEmpty(groupingMap.get(ext.getId())) )
					{
						groupExtensions.add(ext);
					}
				}

				return Iterables.transform(groupExtensions, new Function<Extension, Option<Void>>()
				{
					@Override
					public Option<Void> apply(Extension ext)
					{
						return new VoidKeyOption(ext.getParameter("nameKey").valueAsString(), ext.getId());
					}
				});
			}

			@Override
			protected Iterable<Void> populateModel(SectionInfo info)
			{
				return null;
			}

		};
		listModel.setSort(true);
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
		List<Triple<HtmlLinkState, Label, String>> rowsData = Lists.newArrayList();
		Map<AbstractParentSettingsSection<?>, Set<String>> settingsMap = getSettingsMap();

		// Build row data
		for( final Entry<AbstractParentSettingsSection<?>, Set<String>> setting : settingsMap.entrySet() )
		{
			final AbstractParentSettingsSection<?> sSection = setting.getKey();
			if( sSection.canView(context) )
			{
				final Pair<HtmlLinkState, Label> link = sSection.getLink(context);
				// rowsData.add(new Triple<HtmlLinkState, Label,
				// String>(link.getFirst(), link.getSecond(), Joiner.on(' ')
				// .skipNulls().join(setting.getValue()).trim()));

				rowsData.add(new Triple<HtmlLinkState, Label, String>(link.getFirst(), link.getSecond(), link
					.getSecond().getText()));
			}
		}

		// Sort row data
		Collections.sort(rowsData, new Comparator<Triple<HtmlLinkState, Label, String>>()
		{
			@Override
			public int compare(final Triple<HtmlLinkState, Label, String> t1,
				final Triple<HtmlLinkState, Label, String> t2)
			{
				Label l1 = t1.getFirst().getLabel();
				Label l2 = t2.getFirst().getLabel();

				return l1.getText().compareToIgnoreCase(l2.getText());
			}
		});

		// Build table from row data
		for( final Triple<HtmlLinkState, Label, String> rData : rowsData )
		{
			final CombinedRenderer renderable = new CombinedRenderer(new LinkRenderer(rData.getFirst()),
				new BrRenderer(), new LabelRenderer(rData.getSecond()));
			final TableRow row = settingsTable.addRow(context, renderable);

			final String filterData = rData.getThird();
			if( !Check.isEmpty(filterData) )
			{
				row.setFilterData(filterData);
			}
		}

		settingsTable.makePresentation(context);
	}

	// Map of sections to groups
	public Map<AbstractParentSettingsSection<?>, Set<String>> getSettingsMap()
	{
		Map<AbstractParentSettingsSection<?>, Set<String>> settingsMap = Maps.newHashMap();

		for( Entry<String, Extension> extEntry : settingsExtensions.getExtensionMap().entrySet() )
		{
			Extension settingExt = extEntry.getValue();
			Collection<Parameter> groupParams = settingExt.getParameters("grouping");
			AbstractParentSettingsSection<?> bean = settingsExtensions.getBeanByExtension(settingExt);
			if( !Check.isEmpty(groupParams) )
			{
				for( Parameter groupParam : groupParams )
				{
					Set<String> groups = settingsMap.get(bean);
					if( groups == null )
					{
						groups = Sets.newHashSet();
						settingsMap.put(bean, groups);
					}
					groups.add(groupParam.valueAsString());
				}
			}
			else
			{
				settingsMap.put(bean, new HashSet<String>());
			}
		}

		return settingsMap;
	}

	// Map of group to list of viewable sections
	private Map<String, List<AbstractParentSettingsSection<?>>> getGroupingMap(SectionInfo info)
	{
		Map<String, List<AbstractParentSettingsSection<?>>> groupingMap = Maps.newHashMap();

		// For each extension get the groups it has and build a list of
		// viewable sections for each group
		for( Entry<String, Extension> extEntry : settingsExtensions.getExtensionMap().entrySet() )
		{
			Extension settingExt = extEntry.getValue();

			Collection<Parameter> groups = settingExt.getParameters("grouping");
			for( Parameter groupParam : groups )
			{
				List<AbstractParentSettingsSection<?>> sections = groupingMap.get(groupParam.valueAsString());
				if( sections == null )
				{
					sections = Lists.newArrayList();
					groupingMap.put(groupParam.valueAsString(), sections);
				}

				AbstractParentSettingsSection<?> section = settingsExtensions.getBeanByExtension(settingExt);
				if( section.canView(info) )
				{
					sections.add(section);
				}
			}
		}

		return groupingMap;
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

	// public SingleSelectionList<Void> getFilterButtons()
	// {
	// return filterButtons;
	// }
}
