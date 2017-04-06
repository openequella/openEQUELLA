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
