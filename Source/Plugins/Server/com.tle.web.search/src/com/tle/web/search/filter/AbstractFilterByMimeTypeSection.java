package com.tle.web.search.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.beans.system.SearchSettings;
import com.tle.beans.system.SearchSettings.SearchFilter;
import com.tle.common.NameValue;
import com.tle.core.services.config.ConfigurationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;

public abstract class AbstractFilterByMimeTypeSection<SE extends AbstractSearchEvent<SE>>
	extends
		AbstractPrototypeSection<Object> implements HtmlRenderer, SearchEventListener<SE>, ResetFiltersListener
{
	@Inject
	protected ConfigurationService configService;

	@ViewFactory
	protected FreemarkerFactory viewFactory;

	@TreeLookup
	protected AbstractSearchResultsSection<?, ?, ?, ?> searchResults;

	@Component(name = "mt", parameter = "mt", supported = true)
	protected MultiSelectionList<NameValue> mimeTypes;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		mimeTypes.setEventHandler(JSHandler.EVENT_CHANGE,
			new StatementHandler(searchResults.getRestartSearchHandler(tree)));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( mimeTypes.getListModel().getOptions(context).size() > 0 )
		{
			return viewFactory.createResult("filter/filterbymimetype.ftl", context); //$NON-NLS-1$
		}
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		mimeTypes.setListModel(new DynamicHtmlListModel<NameValue>()
		{
			@Override
			protected Iterable<NameValue> populateModel(SectionInfo info)
			{
				List<SearchFilter> filters = Lists.newArrayList(getSearchSettings().getFilters());
				Collections.sort(filters, new Comparator<SearchFilter>()
				{
					@Override
					public int compare(SearchFilter sf1, SearchFilter sf2)
					{
						return sf1.getName().compareTo(sf2.getName());
					}
				});

				return Lists.transform(filters, new Function<SearchFilter, NameValue>()
				{
					@Override
					public NameValue apply(SearchFilter filter)
					{
						return new NameValue(filter.getName(), filter.getId());
					}
				});
			}
		});
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbmt"; //$NON-NLS-1$
	}

	public MultiSelectionList<NameValue> getMimeTypes()
	{
		return mimeTypes;
	}

	@Override
	public void reset(SectionInfo info)
	{
		mimeTypes.setSelectedStringValue(info, null);
	}

	protected SearchSettings getSearchSettings()
	{
		return configService.getProperties(new SearchSettings());
	}
}