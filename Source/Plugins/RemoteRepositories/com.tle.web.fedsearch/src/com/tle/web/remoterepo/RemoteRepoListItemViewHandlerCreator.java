package com.tle.web.remoterepo;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

/**
 * @author aholland
 */
@TreeIndexed
public interface RemoteRepoListItemViewHandlerCreator<E extends RemoteRepoListEntry<?>> extends SectionId
{
	Bookmark getViewHandler(SectionInfo info, E listItem);
}
