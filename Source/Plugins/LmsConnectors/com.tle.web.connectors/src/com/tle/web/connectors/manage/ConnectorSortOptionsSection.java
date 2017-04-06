package com.tle.web.connectors.manage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SortField;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.core.connectors.service.ExternalContentSortTypeKeys;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.sort.SortOption;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;

@TreeIndexed
public class ConnectorSortOptionsSection extends AbstractPrototypeSection<ConnectorSortOptionsSection.SortOptionsModel>
	implements
		SearchEventListener<ConnectorManagementSearchEvent>,
		HtmlRenderer
{
	@ViewFactory(fixed = true)
	protected FreemarkerFactory viewFactory;
	@EventFactory
	protected EventGenerator events;

	@TreeLookup
	private AbstractSearchResultsSection<?, ?, ?, ?> searchResults;
	@TreeLookup
	private ConnectorManagementQuerySection querySection;
	@Inject
	private ConnectorRepositoryService repositoryService;

	@PlugKey("sortsection.results.reverse")
	private static Label LABEL_REVERSE;

	@Component(name = "so", parameter = "sort", supported = true)
	protected SingleSelectionList<ExternalContentSortType> sortOptions;

	@Component(name = "r", parameter = "reverse", supported = true)
	protected Checkbox reverse;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		sortOptions.setListModel(new ExternalContentSortTypeListModel());
		sortOptions.setAlwaysSelect(true);
		reverse.setLabel(LABEL_REVERSE);
		tree.setLayout(id, SearchResultsActionsSection.AREA_SORT);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		sortOptions.addChangeEventHandler(searchResults.getRestartSearchHandler(tree));
		reverse.setClickHandler(new StatementHandler(searchResults.getResultsUpdater(tree, null)));
	}

	@Override
	public void prepareSearch(SectionInfo info, ConnectorManagementSearchEvent event) throws Exception
	{
		if( !getModel(info).isDisabled() )
		{
			ExternalContentSortType sort = sortOptions.getSelectedValue(info);
			if( sort != null )
			{
				ConnectorContentSearch search = event.getSearch();
				search.setReverse(reverse.isChecked(info));
				search.setSort(sort);
			}
		}
	}

	protected SortField[] createSortFromOption(SectionInfo info, SortOption selOpt)
	{
		return selOpt.createSort();
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !getModel(context).isDisabled() )
		{
			return viewFactory.createResult("sort/sortoptions.ftl", context); //$NON-NLS-1$
		}
		return null;
	}

	public Checkbox getReverse()
	{
		return reverse;
	}

	protected void addSortOptions(SectionInfo info, List<ExternalContentSortType> sorts)
	{
		Connector connector = querySection.getConnector(info);
		if( connector != null )
		{
			sorts.add(ExternalContentSortType.DATE_ADDED);
			sorts.add(ExternalContentSortType.NAME);
			if( repositoryService.supportsCourses(connector.getLmsType()) )
			{
				sorts.add(ExternalContentSortType.COURSE);
			}
		}
	}

	public SingleSelectionList<ExternalContentSortType> getSortOptions()
	{
		return sortOptions;
	}

	public void disable(SectionInfo info)
	{
		getModel(info).setDisabled(true);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SortOptionsModel();
	}

	public class ExternalContentSortTypeListModel extends DynamicHtmlListModel<ExternalContentSortType>
	{
		@Override
		protected Option<ExternalContentSortType> convertToOption(SectionInfo info, ExternalContentSortType obj)
		{
			return new KeyOption<ExternalContentSortType>(ExternalContentSortTypeKeys.get(obj), obj.name()
				.toLowerCase(), obj);
		}

		@Override
		protected Iterable<ExternalContentSortType> populateModel(SectionInfo info)
		{
			List<ExternalContentSortType> sorts = new ArrayList<ExternalContentSortType>();
			addSortOptions(info, sorts);
			return sorts;
		}
	}

	public static class SortOptionsModel
	{
		private boolean disabled;

		public boolean isDisabled()
		{
			return disabled;
		}

		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}
	}
}
