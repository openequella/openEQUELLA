package com.tle.web.sections.generic;

import java.util.Map;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.PathGenerator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.BookmarkEvent;

/**
 * A {@link Bookmark} implementation which generates a URI based on a
 * {@link BookmarkEvent}.
 * 
 * @author jmaginnis
 */
public class InfoBookmark implements Bookmark
{
	protected SectionInfo info;
	private String href;
	private String query;
	private String path;
	private Map<String, String[]> bookmarkParams;
	private BookmarkEvent bookmarkEvent;

	public InfoBookmark(SectionInfo info)
	{
		this(info, new BookmarkEvent());
	}

	public InfoBookmark(SectionInfo info, BookmarkEvent bookmarkEvent)
	{
		this.info = info;
		this.bookmarkEvent = bookmarkEvent;
	}

	@Override
	public String getHref()
	{
		if( href == null )
		{
			String path = getPath();
			String query = getQuery();

			int queryLength = query.length();
			if( queryLength == 0 )
			{
				return path;
			}
			href = path + "?" + query; //$NON-NLS-1$
		}
		return href;
	}

	public String getPath()
	{
		if( path == null )
		{
			PathGenerator pathGen = info.getPathGenerator();
			path = pathGen.getFullURI(info).toString();
		}
		return path;
	}

	public String getQuery()
	{
		if( query == null )
		{
			query = SectionUtils.getParameterString(SectionUtils.getParameterNameValues(getBookmarkParams(), false));
		}
		return query;
	}

	public Map<String, String[]> getBookmarkParams()
	{
		if( bookmarkParams == null )
		{
			info.processEvent(bookmarkEvent);
			bookmarkParams = bookmarkEvent.getBookmarkState();
		}
		return bookmarkParams;
	}

	public void setQuery(String query)
	{
		this.query = query;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public SectionInfo getInfo()
	{
		return info;
	}
}
