package com.tle.web.remoterepo.merlot;

import java.util.Collection;

import com.tle.beans.search.MerlotSettings;
import com.tle.common.NameValue;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface MerlotWebService
{
	MerlotSettings getSettings(SectionInfo info);

	/**
	 * @param categoryId blank or null for root categories.
	 */
	Collection<NameValue> getCategories(SectionInfo info, String categoryId);

	Collection<NameValue> getCommunities(SectionInfo info);

	Collection<NameValue> getLanguages(SectionInfo info);

	Collection<NameValue> getMaterialTypes(SectionInfo info);

	Collection<NameValue> getTechnicalFormats(SectionInfo info);

	Collection<NameValue> getAudiences(SectionInfo info);

	String lookupLanguage(SectionInfo info, String langCode);
}
