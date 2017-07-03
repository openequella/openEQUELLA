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

package com.tle.web.harvestlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.google.inject.Inject;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.common.searching.SearchResults;
import com.tle.core.freetext.service.FreeTextService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

@Bind
public class HarvestListSection extends AbstractPrototypeSection<HarvestListSection.HarvestListModel>
	implements
		HtmlRenderer
{
	private static final int MAX_PER_PAGE = 50;

	@ViewFactory
	private FreemarkerFactory freemarkerFactory;

	@Inject
	private FreeTextService freeTextService;
	@Inject
	private ItemService itemService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ViewItemUrlFactory viewItemUrlFactory;

	@Inject(optional = true)
	@Named("where")
	private String where;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		context.getPreRenderContext().addHeaderMarkup("<meta name=\"robots\" content=\"noindex, follow\">"); //$NON-NLS-1$
		final SectionInfo info = context.getInfo();
		final HarvestListModel model = getModel(context);

		final DefaultSearch search = new DefaultSearch();
		if( where != null )
		{
			search.setFreeTextQuery(WhereParser.parse(where));
		}

		final SearchResults<ItemIdKey> results = freeTextService.searchIds(search,
			model.getCurrentPage() * MAX_PER_PAGE, MAX_PER_PAGE);
		final List<ItemIdKey> ids = results.getResults();

		final Map<ItemId, Long> names = itemService.getItemNameIds(ids);

		// Ensure our names are cached
		bundleCache.addBundleIds(names.values());

		final List<HarvestListRow> rows = new ArrayList<HarvestListRow>(ids.size());
		for( ItemIdKey id : ids )
		{
			ItemId itemId = ItemId.fromKey(id);
			ViewItemUrl url = viewItemUrlFactory.createItemUrl(info, itemId);
			rows.add(new HarvestListRow(url.getHref(), names.get(itemId), itemId.toString()));
		}

		model.setTotalPages((int) Math.ceil(((double) results.getAvailable()) / MAX_PER_PAGE));
		model.setResults(rows);

		Decorations decorations = Decorations.getDecorations(info);
		decorations.clearAllDecorations();
		decorations.setTitle(new TextLabel("EQUELLA"));

		return freemarkerFactory.createResult("harvestlist.ftl", context); //$NON-NLS-1$
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "hl"; //$NON-NLS-1$
	}

	@Override
	public Class<HarvestListModel> getModelClass()
	{
		return HarvestListModel.class;
	}

	public static class HarvestListModel
	{
		@Bookmarked
		private int currentPage = 0;
		private int totalPages;
		private List<HarvestListRow> results;

		public int getCurrentPage()
		{
			return currentPage;
		}

		public void setCurrentPage(int currentPage)
		{
			this.currentPage = currentPage;
		}

		public int getTotalPages()
		{
			return totalPages;
		}

		public void setTotalPages(int totalPages)
		{
			this.totalPages = totalPages;
		}

		public List<HarvestListRow> getResults()
		{
			return results;
		}

		public void setResults(List<HarvestListRow> results)
		{
			this.results = results;
		}
	}

	public static class HarvestListRow
	{
		private final String url;
		private final Long bundleId;
		private final String itemId;

		public HarvestListRow(String url, Long bundleId, String itemId)
		{
			this.url = url;
			this.bundleId = bundleId;
			this.itemId = itemId;
		}

		public String getUrl()
		{
			return url;
		}

		public Long getBundleId()
		{
			return bundleId;
		}

		public String getItemId()
		{
			return itemId;
		}
	}
}
