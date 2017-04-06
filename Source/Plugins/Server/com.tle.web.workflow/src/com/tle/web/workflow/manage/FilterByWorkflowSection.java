package com.tle.web.workflow.manage;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.searching.SortField;
import com.tle.common.searching.SortField.Type;
import com.tle.core.guice.Bind;
import com.tle.core.services.entity.WorkflowService;
import com.tle.core.workflow.freetext.TasksIndexer;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.i18n.BundleCache;
import com.tle.web.itemadmin.section.ItemAdminFilterByItemStatusSection;
import com.tle.web.itemadmin.section.ItemAdminQuerySection;
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
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

@Bind
public class FilterByWorkflowSection extends AbstractPrototypeSection<Object>
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
		workflowList.setListModel(new WorkflowListModel(workflowService, bundleCache));
		extraSortOptions = ImmutableList.of(new SortOption(LABEL_TIMEINMOD, "timeinmod", new SortField(
			TasksIndexer.FIELD_STARTWORKFLOW, false, Type.LONG)));

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		workflowList.addChangeEventHandler(searchResults.getRestartSearchHandler(tree));
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

	private boolean isShowing(SectionInfo info)
	{
		return itemStatus.getOnlyInModeration().isChecked(info)
			&& itemAdminQuery.getCollectionList().getSelectedValue(info) == null;
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
	public Iterable<SortOption> addSortOptions(SectionInfo info, AbstractSortOptionsSection section)
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
}
