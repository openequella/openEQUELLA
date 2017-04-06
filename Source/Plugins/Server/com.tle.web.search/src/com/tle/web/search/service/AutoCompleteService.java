package com.tle.web.search.service;

import com.tle.common.search.DefaultSearch;

public interface AutoCompleteService
{
	AutoCompleteResult[] getAutoCompleteResults(DefaultSearch request, String queryText);
}
