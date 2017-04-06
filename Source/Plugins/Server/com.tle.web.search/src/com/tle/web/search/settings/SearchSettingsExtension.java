package com.tle.web.search.settings;

import com.tle.beans.system.SearchSettings;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface SearchSettingsExtension extends SectionId
{
	void save(SectionInfo info, SearchSettings settings);
}
