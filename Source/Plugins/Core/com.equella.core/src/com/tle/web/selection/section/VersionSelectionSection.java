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

package com.tle.web.selection.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.beans.item.VersionSelection;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.settings.standard.QuickContributeAndVersionSettings;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.MappedLists;
import com.tle.web.sections.standard.MappedStrings;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlValueState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.TargetFolder;

@SuppressWarnings("nls")
@Bind
public class VersionSelectionSection
	extends
		AbstractPrototypeSection<VersionSelectionSection.VersionSelectionTableModel>
	implements HtmlRenderer
{
	private static final String LATEST_VERSION_VALUE = "latest";
	private static final String SPECIFIC_VERSION_VALUE = "specific";

	@PlugKey("checkout.version.latest")
	private static String LATEST_VERSION_KEY;
	@PlugKey("checkout.version.specific")
	private static String SPECIFIC_VERSION_KEY;

	@PlugKey("checkout.table.name")
	private static Label TABLE_NAME_LABEL;
	@PlugKey("checkout.table.version")
	private static Label TABLE_VERSION_LABEL;
	@PlugKey("checkout.table.remove")
	private static Label TABLE_CONFIRM_REMOVE_LABEL;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(name = "t")
	private MappedStrings titles;
	@Component(name = "vs")
	private MappedLists versionSelections;

	private PluginTracker<VersionChoiceDecider> versionChoiceDeciders;
	private JSCallable removeFunc;
	private String ajaxDivId;

	@Inject
	private SelectionService selectionService;
	@Inject
	private ConfigurationService configurationService;
	@Inject
	private ObjectMapperService objectMapperService;

	@Override
	@Nullable
	public SectionResult renderHtml(RenderEventContext context)
	{
		final VersionSelectionTableModel model = getModel(context);
		final VersionSelection versionSelection = getVersionSelection(context);
		final boolean allowVersionChoice = isAllowVersionChoice(versionSelection);

		String defaultVersionOption = null;
		if( allowVersionChoice )
		{
			defaultVersionOption = versionSelection == VersionSelection.DEFAULT_TO_LATEST ? LATEST_VERSION_VALUE
				: SPECIFIC_VERSION_VALUE;
		}

		final String folderId = (Check.isEmpty(model.getParentFolderId()) ? null : model.getParentFolderId());
		final TargetFolder folder = selectionService.findTargetFolder(context, folderId);
		final List<Pair<Label, TableState>> tables = Lists.newArrayList();
		doFolder(context, folder, objectMapperService.createObjectMapper(), allowVersionChoice, defaultVersionOption,
			model.isShowAll(), tables);
		model.setTables(tables);

		return viewFactory.createResult("selection/versionselection.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		removeFunc = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("removeSelection"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), ajaxDivId);
	}

	private void doFolder(SectionInfo info, TargetFolder folder, ObjectMapper mapper, boolean allowVersionChoice,
		String defaultVersionOption, boolean showAll, List<Pair<Label, TableState>> tables)
	{
		if( folder.getResourceCount() > 0 )
		{
			TableState tableState = new TableState();
			tableState.setColumnHeadings(TABLE_NAME_LABEL, allowVersionChoice ? TABLE_VERSION_LABEL : null, null);
			tableState.setColumnSorts(Sort.PRIMARY_ASC, Sort.NONE, Sort.NONE);

			for( SelectedResource res : folder.getResources().values() )
			{
				final String key = getSelectedResourceAsString(mapper, res);

				final HtmlValueState title = titles.getValueState(info, key);
				if( Strings.isNullOrEmpty(title.getValue()) )
				{
					title.setValue(res.getTitle());
				}
				title.addClass("title");

				final TableCell versionCell = new TableCell();
				if( allowVersionChoice )
				{
					String dvo = defaultVersionOption;
					if( res.isVersionChoiceMade() )
					{
						dvo = res.isLatest() ? LATEST_VERSION_VALUE : SPECIFIC_VERSION_VALUE;
					}
					versionCell.addContent(
						versionSelections.getListState(info, key, getVersionOptionsForSelectedResource(res), dvo));
					versionCell.addClasses("version");
				}

				final HtmlComponentState unselect = new HtmlComponentState();
				unselect.setClickHandler(
					new OverrideHandler(removeFunc, key).addValidator(Js.confirm(TABLE_CONFIRM_REMOVE_LABEL)));
				TableCell unselectCell = new TableCell(unselect);
				unselectCell.addClasses("unselect");

				tableState.addRow(title, versionCell, unselectCell);
			}
			tables.add(new Pair<Label, TableState>(new TextLabel(folder.getName()), tableState));
		}

		if( showAll && folder.hasChildrenFolders() )
		{
			for( TargetFolder child : folder.getFolders() )
			{
				doFolder(info, child, mapper, allowVersionChoice, defaultVersionOption, showAll, tables);
			}
		}
	}

	private VersionSelection getVersionSelection(SectionInfo info)
	{
		VersionSelection vs = null;
		final SelectionSession ss = selectionService.getCurrentSession(info);
		vs = ss.getOverrideVersionSelection();

		if( vs == null )
		{
			final Iterator<VersionChoiceDecider> i = versionChoiceDeciders.getBeanList().iterator();
			while( vs == null && i.hasNext() )
			{
				vs = i.next().getVersionSelection(info);

				// Sanity check. This can be removed once INSTITUTION_DEFAULT
				// has been deleted and the course data migrated.
				if( vs == VersionSelection.INSTITUTION_DEFAULT )
				{
					throw new SectionsRuntimeException(
						"Deprecated value returned - implementation should be returning null for this case");
				}
			}
			if( vs == null )
			{
				vs = configurationService.getProperties(new QuickContributeAndVersionSettings()).getVersionSelection();

				if( vs == null )
				{
					vs = VersionSelection.FORCE_CURRENT;
				}
			}
		}
		return vs;
	}

	private boolean isAllowVersionChoice(VersionSelection vs)
	{
		return vs == VersionSelection.DEFAULT_TO_CURRENT || vs == VersionSelection.DEFAULT_TO_LATEST;
	}

	private List<Option<?>> getVersionOptionsForSelectedResource(SelectedResource res)
	{
		List<Option<?>> rv = new ArrayList<Option<?>>();
		rv.add(new VoidKeyOption(LATEST_VERSION_KEY, LATEST_VERSION_VALUE));
		rv.add(new VoidKeyOption(SPECIFIC_VERSION_KEY, new Object[]{res.getVersion()}, SPECIFIC_VERSION_VALUE));
		return rv;
	}

	@EventHandlerMethod
	public void removeSelection(SectionInfo info, SelectedResourceKey selectionKey)
	{
		selectionService.removeSelectedResource(info, selectionKey);
	}

	public void saveVersionChoices(SectionInfo info)
	{
		VersionSelectionTableModel model = getModel(info);
		VersionSelection versionSelection = getVersionSelection(info);

		final ObjectMapper mapper = objectMapperService.createObjectMapper();
		final TargetFolder folder;
		Collection<SelectedResource> selections;

		if( model.isShowAll() )
		{
			folder = selectionService.findTargetFolder(info, null);
			selections = folder.getAllResources().values();
		}
		else
		{
			folder = selectionService.findTargetFolder(info, model.getParentFolderId());
			selections = folder.getResources().values();
		}
		final Map<String, String> titlesMap = titles.getValuesMap(info);

		for( SelectedResource res : selections )
		{
			final String key = getSelectedResourceAsString(mapper, res);

			res.setVersionChoiceMade(true);
			if( isAllowVersionChoice(versionSelection) )
			{
				String v = versionSelections.getSelectedValue(info, key);
				res.setLatest(LATEST_VERSION_VALUE.equals(v));
			}
			else
			{
				res.setLatest(versionSelection == VersionSelection.FORCE_LATEST);
			}
			
			final String title = titlesMap.get(key);
			if( !Strings.isNullOrEmpty(title) )
			{
				res.setTitle(title);
			}
		}
	}

	private String getSelectedResourceAsString(ObjectMapper mapper, SelectedResource res)
	{
		try
		{
			return mapper.writeValueAsString(res.getKey());
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	public void setFolder(SectionInfo info, String folderId, boolean showAll)
	{
		final VersionSelectionTableModel model = getModel(info);
		model.setParentFolderId(folderId);
		model.setShowAll(showAll);
	}

	@Override
	public VersionSelectionTableModel instantiateModel(SectionInfo info)
	{
		return new VersionSelectionTableModel();
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		versionChoiceDeciders = new PluginTracker<VersionChoiceDecider>(pluginService, "com.tle.web.selection",
			"versionChoiceDecider", null);
		versionChoiceDeciders.setBeanKey("class");
	}

	public void setAjaxDivId(String ajaxDivId)
	{
		this.ajaxDivId = ajaxDivId;
	}

	public static class VersionSelectionTableModel
	{
		@Bookmarked(name = "f")
		private String parentFolderId;
		@Bookmarked(name = "a")
		private boolean showAll;

		private List<Pair<Label, TableState>> tables;

		public String getParentFolderId()
		{
			return parentFolderId;
		}

		public void setParentFolderId(String parentFolderId)
		{
			this.parentFolderId = parentFolderId;
		}

		public boolean isShowAll()
		{
			return showAll;
		}

		public void setShowAll(boolean showAll)
		{
			this.showAll = showAll;
		}

		public List<Pair<Label, TableState>> getTables()
		{
			return tables;
		}

		public void setTables(List<Pair<Label, TableState>> tables)
		{
			this.tables = tables;
		}
	}
}
