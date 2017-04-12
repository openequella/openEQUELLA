package com.tle.mypages.parse.conversion;

import org.ccil.cowan.tagsoup.AttributesImpl;

import com.tle.web.sections.js.JSUtils;

public class RawAnchorReplacementConversion implements HrefConversion
{
	@SuppressWarnings("nls")
	@Override
	public String convert(String href, AttributesImpl atts)
	{
		if( href.startsWith("#") )
		{
			String onclick = "document.location.hash = " + JSUtils.toJSString(href.substring(1)) + "; return false;";
			int index = atts.getIndex("onclick");
			if( index == -1 )
			{
				atts.addAttribute("", "onclick", "onclick", "CDATA", onclick);
			}
			else
			{
				atts.setValue(index, onclick);
			}
		}
		return href;
	}
}
