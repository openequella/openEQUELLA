package com.tle.web.selection.home.model;

import java.util.List;

import com.tle.web.selection.home.model.RecentSelectionSegmentModel.RecentSelection;

public class RecentSegmentModel
{
	private List<RecentSelection> recent;

	public void setRecent(List<RecentSelection> recent)
	{
		this.recent = recent;
	}

	public List<RecentSelection> getRecent()
	{
		return recent;
	}
}
