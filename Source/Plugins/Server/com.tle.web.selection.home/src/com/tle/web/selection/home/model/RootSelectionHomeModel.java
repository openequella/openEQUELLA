package com.tle.web.selection.home.model;

import java.util.List;

import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;

public class RootSelectionHomeModel
{
	private TemplateResult sections;
	private List<RecentSelectionSegmentModel> recentSegments;
	private List<SectionRenderable> quickSections;
	private boolean quickSearch;
	private String errorKey;

	public TemplateResult getSections()
	{
		return sections;
	}

	public void setSections(TemplateResult sections)
	{
		this.sections = sections;
	}

	public List<RecentSelectionSegmentModel> getRecentSegments()
	{
		return recentSegments;
	}

	public void setRecentSegments(List<RecentSelectionSegmentModel> recentSegments)
	{
		this.recentSegments = recentSegments;
	}

	public boolean isQuickSearch()
	{
		return quickSearch;
	}

	public void setQuickSearch(boolean quickSearch)
	{
		this.quickSearch = quickSearch;
	}

	public String getErrorKey()
	{
		return errorKey;
	}

	public void setErrorKey(String errorKey)
	{
		this.errorKey = errorKey;
	}

	public List<SectionRenderable> getQuickSections()
	{
		return quickSections;
	}

	public void setQuickSections(List<SectionRenderable> quickSections)
	{
		this.quickSections = quickSections;
	}
}