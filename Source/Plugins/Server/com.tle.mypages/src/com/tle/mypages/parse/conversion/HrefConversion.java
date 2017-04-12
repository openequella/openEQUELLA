package com.tle.mypages.parse.conversion;

import org.ccil.cowan.tagsoup.AttributesImpl;

public interface HrefConversion
{
	String convert(String href, AttributesImpl atts);
}
