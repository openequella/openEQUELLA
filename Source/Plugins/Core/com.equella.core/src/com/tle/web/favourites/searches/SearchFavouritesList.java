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

package com.tle.web.favourites.searches;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.common.Check;
import com.tle.core.favourites.bean.FavouriteSearch;
import com.tle.core.favourites.service.FavouriteSearchService;
import com.tle.core.guice.Bind;
import com.tle.web.itemlist.StandardListSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

@TreeIndexed
@Bind
public class SearchFavouritesList
	extends
		StandardListSection<FavouriteSearchEntry, StandardListSection.Model<FavouriteSearchEntry>>
{
	@PlugKey("favsearch.query")
	private static Label QUERY_LABEL;
	@PlugKey("favsearch.within")
	private static Label WITHIN_LABEL;
	@PlugKey("favsearch.criteria")
	private static Label CRITERIA_LABEL;
	@PlugKey("favsearch.delete")
	private static Label DELETE_LABEL;
	@PlugKey("favsearch.deleteconfirm")
	private static Confirm DELETE_CONFIRM;
	@PlugKey("favsearch.deletereceipt")
	private static Label DELETE_RECEIPT_LABEL;

	@EventFactory
	private EventGenerator events;

	@Inject
	private FavouriteSearchService favouriteSearchService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private Provider<FavouriteSearchEntry> entryProvider;

	private JSCallable runSearchFunc;
	private JSCallable deleteSearchFunc;

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		runSearchFunc = events.getSubmitValuesFunction("runSearch");
		deleteSearchFunc = events.getSubmitValuesFunction("deleteSearch");
	}

	public void addSearch(SectionInfo info, FavouriteSearch search)
	{
		FavouriteSearchEntry entry = createFavouriteSearchEntry();
		entry.setSearchFavouritesList(this);
		entry.setSearch(search);

		addData(entry, search.getQuery(), QUERY_LABEL);
		addData(entry, search.getWithin(), WITHIN_LABEL);
		addData(entry, search.getCriteria(), CRITERIA_LABEL);

		final long searchId = search.getId();
		entry.addRatingAction(new ButtonRenderer(new HtmlLinkState(DELETE_LABEL, new OverrideHandler(deleteSearchFunc,
			searchId).addValidator(DELETE_CONFIRM))).showAs(ButtonType.DELETE));

		addListItem(info, entry);
	}

	private FavouriteSearchEntry createFavouriteSearchEntry()
	{
		return entryProvider.get();
	}

	@EventHandlerMethod
	public void runSearch(SectionInfo info, long id)
	{
		favouriteSearchService.executeSearch(info, id);
	}

	@EventHandlerMethod
	public void deleteSearch(SectionInfo info, long id)
	{
		favouriteSearchService.deleteById(id);
		receiptService.setReceipt(DELETE_RECEIPT_LABEL);
	}

	private void addData(FavouriteSearchEntry entry, String val, Label name)
	{
		if( !Check.isEmpty(val) )
		{
			entry.addDelimitedMetadata(name, val);
		}
	}

	public JSCallable getRunSearchFunc()
	{
		return runSearchFunc;
	}
}
