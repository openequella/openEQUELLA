package com.tle.web.searching.section;

import com.tle.web.sections.SectionInfo;

/**
 * This subclass's sole raison d'etre is to provide a alternative for
 * createForward(..) with a skinny URL rather than a fat URL. Given its only
 * features are static, extending RootSearchSection is for reference only.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public class RootSkinnySearchSection extends RootSearchSection
{
	public static final String SKINNYSEARCHURL = "/access/skinny/searching.do";

	public static SectionInfo createForward(SectionInfo from)
	{
		return from.createForward(SKINNYSEARCHURL);
	}
}
