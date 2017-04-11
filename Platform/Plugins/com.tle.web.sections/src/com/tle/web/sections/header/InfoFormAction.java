package com.tle.web.sections.header;

import java.util.Map;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.EventAuthoriser;
import com.tle.web.sections.generic.InfoBookmark;

public class InfoFormAction implements FormAction
{
	private InfoBookmark bookmark;
	private Map<String, String[]> hiddenState;

	public InfoFormAction(InfoBookmark bookmark)
	{
		this.bookmark = bookmark;
	}

	@Override
	public String getFormAction()
	{
		return bookmark.getPath();
	}

	@Override
	public Map<String, String[]> getHiddenState()
	{
		if( hiddenState == null )
		{
			hiddenState = bookmark.getBookmarkParams();
			SectionInfo info = bookmark.getInfo();
			EventAuthoriser authoriser = info.getAttributeForClass(EventAuthoriser.class);
			if( authoriser != null )
			{
				authoriser.addToBookmark(info, hiddenState);
			}
		}
		return hiddenState;
	}

}
