package com.tle.web.sections.equella.layout;

import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public class OuterLayout
{
	private static final String OUTER_LAYOUT_KEY = "$OUTER_LAYOUT$"; //$NON-NLS-1$

	public static final OuterLayout STANDARD = new OuterLayout("layouts/outer/standard.ftl"); //$NON-NLS-1$

	private final String ftl;

	public OuterLayout(String ftl)
	{
		this.ftl = ftl;
	}

	public String getFtl()
	{
		return ftl;
	}

	public static void setLayout(SectionInfo info, OuterLayout layout)
	{
		if( layout == null )
		{
			throw new IllegalArgumentException("layout cannot be null"); //$NON-NLS-1$
		}
		info.setAttribute(OUTER_LAYOUT_KEY, layout);
	}

	/**
	 * The current outer layout (frameset, standard).
	 * 
	 * @param info
	 * @return Will never return null, default layout is OuterLayout.STANDARD
	 */
	public static OuterLayout getLayout(SectionInfo info)
	{
		OuterLayout layout = info.getAttribute(OUTER_LAYOUT_KEY);
		if( layout == null )
		{
			layout = OuterLayout.STANDARD;
			setLayout(info, layout);
		}
		return layout;
	}
}
