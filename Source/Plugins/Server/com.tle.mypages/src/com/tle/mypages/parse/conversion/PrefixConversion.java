package com.tle.mypages.parse.conversion;

import org.ccil.cowan.tagsoup.AttributesImpl;

/**
 * 
 */
public class PrefixConversion implements HrefConversion
{
	protected final String sourcePrefix;
	protected final String destPrefix;

	public PrefixConversion(String src, String dest)
	{
		sourcePrefix = src;
		destPrefix = dest;
	}

	@Override
	public String convert(String href, AttributesImpl atts)
	{
		if( href.startsWith(sourcePrefix) )
		{
			return destPrefix + href.substring(sourcePrefix.length());
		}
		return href;
	}
}