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

package com.tle.web.workflow.manage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.searching.SortField;
import com.tle.common.searching.SortField.Type;
import com.tle.common.workflow.Workflow;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.workflow.freetext.TasksIndexer;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemadmin.section.ItemAdminFilterByItemStatusSection;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
import com.tle.web.itemadmin.section.ItemDefinitionSelection;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.search.sort.AbstractSortOptionsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.search.sort.SortOptionsListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

@Bind
public class FilterByWorkflowSection
	extends
		AbstractPrototypeSection<FilterByWorkflowSection.FilterByWorkflowForCollectionModel>
	implements
		HtmlRenderer,
		SearchEventListener<FreetextSearchEvent>,
		ResetFiltersListener,
		SortOptionsListener
{
	@PlugKey("sort.workflow.inmod")
	private static Label LABEL_TIMEINMOD;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private WorkflowService workflowService;

	@Component(parameter = "workflow", supported = true)
	private SingleSelectionList<BaseEntityLabel> workflowList;

	@TreeLookup
	private ItemDefinitionSelection selectedCollection;
	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@TreeLookup
	private ItemAdminFilterByItemStatusSection itemStatus;
	@TreeLookup
	private ItemAdminQuerySection itemAdminQuery;

	private ImmutableList<SortOption> extraSortOptions;

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		workflowList.setListModel(new WorkflowListModel(workflowService, bundleCache)
		{
			@Override
			public Iterable<BaseEntityLabel> populateModel(SectionInfo info)
			{
				List<BaseEntityLabel> workflowOptions = new ArrayList<BaseEntityLabel>();

				Collection<BaseEntityLabel> listManagable = workflowService.listManagable();

				ItemDefinition collection = getCollection(info);

				Workflow workflowForCollection;

				if( collection != null )
				{
					workflowForCollection = collection.getWorkflow();
					if( workflowForCollection == null )
					{
						return workflowOptions;
					}

					workflowOptions.add(new BaseEntityLabel(workflowForCollection.getId(), workflowForCollection.getUuid(),
						workflowForCollection.getName().getId(), workflowForCollection.getOwner(), workflowForCollection
							.isSystemType()));

				}

				else
				{
					for( BaseEntityLabel bel : listManagable )
					{
						workflowOptions.add(bel);
					}
				}

				return workflowOptions;
			}
		});
		extraSortOptions = ImmutableList.of(new SortOption(LABEL_TIMEINMOD, "timeinmod", new SortField(
			TasksIndexer.FIELD_STARTWORKFLOW, false, Type.LONG)));

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		workflowList.addChangeEventHandler(
			new StatementHandler(searchResults.getResultsUpdater(tree, null, "searchresults-actions")));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !isShowing(context) )
		{
			return null;
		}

		return viewFactory.createResult("filterbyworkflow.ftl", this); //$NON-NLS-1$
	}

	public boolean isShowing(SectionInfo info)
	{
		Collection<BaseEntityLabel> listManagable = workflowService.listManagable();
		boolean emptyList = listManagable.isEmpty();
		return itemStatus.getOnlyInModeration().isChecked(info) && !emptyList;
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		if( isShowing(info) )
		{
			BaseEntityLabel selectedValue = workflowList.getSelectedValue(info);
			if( selectedValue != null )
			{
				event.filterByTerm(false, TasksIndexer.FIELD_WORKFLOW, selectedValue.getUuid());
			}
		}
	}

	@Nullable
	private ItemDefinition getCollection(SectionInfo info)
	{
		FilterByWorkflowForCollectionModel model = getModel(info);
		ItemDefinition collection = model.getCollection();
		if( collection == null )
		{
			collection = selectedCollection.getCollection(info);
			model.setCollection(collection);
		}

		return collection;
	}

	public String getWorkflowUuid(SectionInfo info)
	{
		BaseEntityLabel selectedValue = workflowList.getSelectedValue(info);
		if( selectedValue != null )
		{
			return selectedValue.getUuid();

		}
		return null;
	}

	public boolean isWorkflowSelected(SectionInfo info)
	{
		if( isShowing(info) )
		{
			BaseEntityLabel selectedValue = workflowList.getSelectedValue(info);
			if( selectedValue != null )
			{
				return true;
			}
		}
		return false;
	}

	public SingleSelectionList<BaseEntityLabel> getWorkflowList()
	{
		return workflowList;
	}

	@Override
	public void reset(SectionInfo info)
	{
		workflowList.setSelectedValue(info, null);
	}

	@Override
	public Class<FilterByWorkflowForCollectionModel> getModelClass()
	{
		return FilterByWorkflowForCollectionModel.class;
	}

	@Override
	public Iterable<SortOption> addSortOptions(SectionInfo info, AbstractSortOptionsSection<?> section)
	{
		if( itemStatus.getOnlyInModeration().isChecked(info) )
		{
			return extraSortOptions;
		}
		return null;
	}

	public void setWorkflow(SectionInfo info, String workflowUuid)
	{
		workflowList.setSelectedStringValue(info, workflowUuid);
	}

	public static class FilterByWorkflowForCollectionModel
	{

		private ItemDefinition collection;

		public ItemDefinition getCollection()
		{
			return collection;
		}

		public void setCollection(ItemDefinition collection)
		{
			this.collection = collection;
		}

	}
}
