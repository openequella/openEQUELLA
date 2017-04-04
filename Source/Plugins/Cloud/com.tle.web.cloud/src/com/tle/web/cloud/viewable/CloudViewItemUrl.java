package com.tle.web.cloud.viewable;

import com.tle.core.services.UrlService;
import com.tle.encoding.UrlEncodedString;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewItemUrl;

/**
 * @author Aaron
 */
public class CloudViewItemUrl extends ViewItemUrl
{
	public CloudViewItemUrl(SectionInfo info, String itemdir, UrlEncodedString filepath, UrlService urlService,
		int flags)
	{
		super(info, itemdir, filepath, null, urlService, flags);
	}
}