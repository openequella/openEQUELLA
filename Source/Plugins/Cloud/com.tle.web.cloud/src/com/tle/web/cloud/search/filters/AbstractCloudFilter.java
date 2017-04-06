package com.tle.web.cloud.search.filters;

import javax.inject.Inject;

import com.tle.common.NameValue;
import com.tle.core.cloud.service.CloudService;
import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.filter.ResetFiltersListener;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlListModel;

@SuppressWarnings("nls")
public abstract class AbstractCloudFilter extends AbstractPrototypeSection<Object>
	implements
		SearchEventListener<CloudSearchEvent>,
		ResetFiltersListener,
		HtmlRenderer
{
	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@Inject
	protected CloudService cloudService;

	@TreeLookup
	protected AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Component(supported = true)
	protected SingleSelectionList<NameValue> list;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		list.setListModel(buildListModel());
		list.setParameterId(getPublicParam());
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		list.addChangeEventHandler(searchResults.getRestartSearchHandler(tree));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("filter/cloudfilter.ftl", context);
	}

	public SingleSelectionList<NameValue> getList()
	{
		return list;
	}

	@Override
	public void reset(SectionInfo info)
	{
		list.setSelectedValue(info, null);
	}

	protected abstract Label getTitle();

	protected abstract HtmlListModel<NameValue> buildListModel();

	protected abstract String getPublicParam();
}
