package com.tle.web.searching.section;

import java.util.Map;

import com.tle.web.search.actions.StandardShareSearchQuerySection;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.generic.InfoBookmark;

@SuppressWarnings("nls")
public class IntegrationShareSearchQuerySection extends StandardShareSearchQuerySection
{
	private static final String SEARCHURL;
	static
	{
		SEARCHURL = RootSearchSection.SEARCHURL.startsWith("/") ? RootSearchSection.SEARCHURL.substring(1)
			: RootSearchSection.SEARCHURL;
	}

	@Override
	public void setupUrl(InfoBookmark bookmark, RenderContext context)
	{
		url.setValue(context, new BookmarkAndModify(bookmark, new BookmarkModifier()
		{
			@Override
			public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
			{
				bookmarkState.put(SectionInfo.KEY_PATH, new String[]{urlService.institutionalise(SEARCHURL)});
			}
		}).getHref());
		url.getState(context).setEditable(false);
	}
}
