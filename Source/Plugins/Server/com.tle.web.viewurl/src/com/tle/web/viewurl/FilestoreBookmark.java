package com.tle.web.viewurl;

import com.tle.beans.item.ItemKey;
import com.tle.common.URLUtils;
import com.tle.core.services.UrlService;
import com.tle.web.sections.Bookmark;

public class FilestoreBookmark implements Bookmark
{
	private final String middle;
	private final String path;
	private final UrlService urlService;
	private final String stagingUuid;

	public FilestoreBookmark(UrlService urlService, ItemKey itemId, String path)
	{
		this.middle = itemId.toString();
		this.urlService = urlService;
		this.path = path;
		this.stagingUuid = null;
	}

	public FilestoreBookmark(UrlService urlService, String stagingId, String path)
	{
		this.middle = URLUtils.urlEncode(stagingId) + "/$"; //$NON-NLS-1$
		this.urlService = urlService;
		this.path = path;
		this.stagingUuid = stagingId;
	}

	@Override
	public String getHref()
	{
		return urlService.institutionalise("file/" + middle + '/' //$NON-NLS-1$
			+ URLUtils.urlEncode(path, false));
	}

	public String getStagingUuid()
	{
		return stagingUuid;
	}
}
