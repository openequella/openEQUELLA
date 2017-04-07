package com.tle.web.sections;

@TreeIndexed
public interface PublicBookmarkFactory extends SectionId
{
	Bookmark getPublicBookmark(SectionInfo info);
}
