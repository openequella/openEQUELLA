package com.tle.core.services.html;

import org.ccil.cowan.tagsoup.AttributesImpl;

public interface HrefCallback
{
	String hrefFound(final String tag, final String url, AttributesImpl atts);

	String textFound(String text);
}
