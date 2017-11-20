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

package com.tle.web.search.filter;

import javax.inject.Inject;

import com.tle.web.search.base.AbstractFreetextResultsSection;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.service.AutoCompleteResult;
import com.tle.web.search.service.AutoCompleteService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;

@SuppressWarnings("nls")
public class FilterByAutoCompleteKeywordSection extends FilterByKeywordSection
{
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private AutoCompleteService autoCompleteService;

	@TreeLookup
	private AbstractFreetextResultsSection<?, ?> searchResults;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		queryField.setAutoCompleteCallback(ajax.getAjaxFunction("updateSearchTerms"));
	}

	@AjaxMethod
	public AutoCompleteResult[] updateSearchTerms(SectionInfo info)
	{
		FreetextSearchEvent fte = searchResults.createSearchEvent(info);
		fte.setExcludeKeywords(true);
		info.processEvent(fte);

		return autoCompleteService.getAutoCompleteResults(fte.getFinalSearch(), queryField.getValue(info));
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "fbakw";
	}
}
