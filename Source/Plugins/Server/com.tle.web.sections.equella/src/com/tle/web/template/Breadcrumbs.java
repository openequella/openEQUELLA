package com.tle.web.template;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TagState;

public class Breadcrumbs
{
	public static final String KEY = "BREADCRUMBS_KEY"; //$NON-NLS-1$

	private final List<TagState> crumbs = new ArrayList<TagState>();
	private Label forcedLastCrumb;

	public List<TagState> getLinks()
	{
		return crumbs;
	}

	public Label getForcedLastCrumb()
	{
		return forcedLastCrumb;
	}

	public void setForcedLastCrumb(Label forcedLastCrumb)
	{
		this.forcedLastCrumb = forcedLastCrumb;
	}

	public void add(TagState crumb)
	{
		crumbs.add(crumb);
	}

	public void addToStart(TagState crumb)
	{
		crumbs.add(0, crumb);
	}

	public static Breadcrumbs get(SectionInfo info)
	{
		Breadcrumbs crumbs = info.getAttribute(KEY);
		if( crumbs == null )
		{
			crumbs = new Breadcrumbs();
			info.setAttribute(KEY, crumbs);
		}
		return crumbs;
	}
}
