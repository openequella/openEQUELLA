package com.tle.core.remoterepo.merlot.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.beans.entity.FederatedSearch;
import com.tle.common.NameValue;
import com.tle.common.searching.SearchResults;
import com.tle.core.remoterepo.merlot.service.impl.MerlotSearchResult;

/**
 * @author agibb
 * @author aholland
 */
public interface MerlotService
{
	SearchResults<MerlotSearchResult> search(MerlotSearchParams search, int offset, int perpage);

	/**
	 * Sadly this is just a re-search
	 * 
	 * @param search
	 * @param index
	 * @return
	 */
	MerlotSearchResult getResult(MerlotSearchParams search, int index);

	Map<String, Collection<NameValue>> getCategories(FederatedSearch merlotSearch);

	List<NameValue> getCommunities(FederatedSearch merlotSearch);

	List<NameValue> getLanguages(FederatedSearch merlotSearch);

	List<NameValue> getMaterialTypes(FederatedSearch merlotSearch);

	List<NameValue> getTechnicalFormats(FederatedSearch merlotSearch);

	List<NameValue> getAudiences(FederatedSearch merlotSearch);

}
