package com.tle.core.remoterepo.merlot.service;

import java.util.Set;

import com.tle.beans.entity.FederatedSearch;
import com.tle.common.util.TleDate;

public interface MerlotSearchParams
{
	public enum KeywordUse
	{
		ALL, ANY, EXACT_PHRASE
	}

	KeywordUse getKeywordUse();

	String getQuery();

	FederatedSearch getMerlotSearch();

	String getCategory();

	String getCommunity();

	String getLanguage();

	String getMaterialType();

	String getTechnicalFormat();

	String getMaterialAudience();

	String getSort();

	boolean isCost();

	boolean isCreativeCommons();

	Set<String> getMobileOS();

	Set<String> getMobileType();

	TleDate getCreatedBefore();

	TleDate getCreatedAfter();
}
