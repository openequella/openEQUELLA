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

package com.tle.web.mimetypes.search.section;

import javax.inject.Inject;

import com.tle.beans.mime.MimeEntry;
import com.tle.common.searching.SearchResults;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.mimetypes.MimeTypesSearchResults;
import com.tle.core.mimetypes.institution.MimeMigrator;
import com.tle.web.mimetypes.search.event.MimeSearchEvent;
import com.tle.web.mimetypes.search.event.MimeSearchResultEvent;
import com.tle.web.mimetypes.search.result.MimeListEntry;
import com.tle.web.mimetypes.search.result.MimeListEntryFactory;
import com.tle.web.mimetypes.search.result.MimeListEntrySection;
import com.tle.web.mimetypes.section.MimeTypesEditSection;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.search.base.AbstractSearchResultsSection.SearchResultsModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;

@SuppressWarnings("nls")
public class MimeResultsSection
	extends
		AbstractSearchResultsSection<MimeListEntry, MimeSearchEvent, MimeSearchResultEvent, SearchResultsModel>
{
	static
	{
		PluginResourceHandler.init(AbstractSearchResultsSection.class);
	}

	@PlugKey("results.title")
	private static Label LABEL_RESULTSTITLE;
	@PlugKey("list.result.deleteconfirm")
	private static Confirm DELETE_CONFIRM;
	@PlugKey("list.result.deletereceipt")
	private static Label LABEL_DELETE_RECEIPT;

	@Inject
	private MimeListEntrySection list;
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private MimeListEntryFactory entryFactory;
	@Inject
	private ReceiptService receiptService;

	@EventFactory
	private EventGenerator events;

	private SubmitValuesFunction editFunc;
	private SubmitValuesFunction deleteFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.registerInnerSection(list, id);

		editFunc = events.getSubmitValuesFunction("edit");
		deleteFunc = events.getSubmitValuesFunction("delete");
	}

	@Override
	protected Label getDefaultResultsTitle(SectionInfo info, MimeSearchEvent searchEvent, MimeSearchResultEvent resultsEvent)
	{
		return LABEL_RESULTSTITLE;
	}

	@Override
	public MimeSearchEvent createSearchEvent(SectionInfo info)
	{
		return new MimeSearchEvent();
	}

	@Override
	public MimeListEntrySection getItemList(SectionInfo info)
	{
		return list;
	}

	@EventHandlerMethod
	public void edit(SectionInfo info, long id) throws Exception
	{
		MimeTypesEditSection.edit(info, id);
	}

	@EventHandlerMethod
	public void delete(SectionInfo info, long id) throws Exception
	{
		mimeTypeService.delete(id);
		receiptService.setReceipt(LABEL_DELETE_RECEIPT);
	}

	@Override
	protected MimeSearchResultEvent createResultsEvent(SectionInfo info, MimeSearchEvent searchEvent)
	{
		MimeTypesSearchResults searchByMimeType = mimeTypeService.searchByMimeType(searchEvent.getQuery(),
			searchEvent.getOffset(), searchEvent.getCount());
		return new MimeSearchResultEvent(searchByMimeType);
	}

	@Override
	public void processResults(SectionInfo info, MimeSearchResultEvent event)
	{
		SearchResults<MimeEntry> results = event.getResults();

		for( MimeEntry mime : results.getResults() )
		{
			list.addListItem(info, entryFactory.createMimeListEntry(info, mime,
				new OverrideHandler(editFunc, mime.getId()), !MimeMigrator.EQUELLA_TYPES.contains(mime.getType())
					? new OverrideHandler(deleteFunc, mime.getId()).addValidator(DELETE_CONFIRM) : null));
		}
	}
}
