package com.tle.web.sections.equella.layout;

import com.tle.web.sections.SectionInfo;

/**
 * @author aholland
 */
public class ContentLayout
{
	private static final String CONTENT_LAYOUT_KEY = "$CONTENT_LAYOUT$"; //$NON-NLS-1$

	public static final ContentLayout ONE_COLUMN = new ContentLayout("layouts/content/onecolumn.ftl"); //$NON-NLS-1$
	public static final ContentLayout TWO_COLUMN = new ContentLayout("layouts/content/twocolumn.ftl"); //$NON-NLS-1$
	public static final ContentLayout COMBINED_COLUMN = new ContentLayout("layouts/content/combinedcolumn.ftl"); //$NON-NLS-1$

	private final String ftl;

	public ContentLayout(String ftl)
	{
		this.ftl = ftl;
	}

	public String getFtl()
	{
		return ftl;
	}

	public static void setLayout(SectionInfo info, ContentLayout layout)
	{
		if( layout == null )
		{
			throw new IllegalArgumentException("layout cannot be null"); //$NON-NLS-1$
		}
		info.setAttribute(CONTENT_LAYOUT_KEY, layout);
	}

	public static ContentLayout getLayout(SectionInfo info)
	{
		return (ContentLayout) info.getAttribute(CONTENT_LAYOUT_KEY);
	}
}
