package com.tle.web.sections.events;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

/**
 * The event listener interface for participating in the {@code Bookmarking}
 * process.
 * 
 * @see BookmarkEvent
 * @author jmaginnis
 */
@NonNullByDefault
public interface BookmarkEventListener extends TargetedEventListener
{
	void bookmark(SectionInfo info, BookmarkEvent event);

	void document(SectionInfo info, DocumentParamsEvent event);
}
