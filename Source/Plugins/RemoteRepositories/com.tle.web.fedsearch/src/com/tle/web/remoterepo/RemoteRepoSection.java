package com.tle.web.remoterepo;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

/**
 * @author aholland
 */
@TreeIndexed
public interface RemoteRepoSection extends SectionId
{
	String getSearchUuid(SectionInfo info);

	void setSearchUuid(SectionInfo info, String searchUuid);
}
