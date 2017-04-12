package com.tle.web.selection.home.model;

import java.util.List;

import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.selection.SelectionHistory;

public class RecentSelectionSegmentModel
{
	private final String title;
	private final List<RecentSelection> selections;

	public RecentSelectionSegmentModel(String title, List<RecentSelection> selections)
	{
		this.title = title;
		this.selections = selections;
	}

	public String getTitle()
	{
		return title;
	}

	public List<RecentSelection> getSelections()
	{
		return selections;
	}

	public static class RecentSelection
	{
		private String title;
		private SelectionHistory resource;
		private final HtmlComponentState link;

		public RecentSelection(SelectionHistory resource, HtmlComponentState link)
		{
			this.resource = resource;
			this.link = link;
		}

		public RecentSelection(String title, HtmlComponentState link)
		{
			this.title = title;
			this.link = link;
		}

		public String getTitle()
		{
			if( resource != null )
			{
				return resource.getTitle();
			}
			return title;
		}

		public void setTitle(String title)
		{
			this.title = title;
		}

		public HtmlComponentState getLink()
		{
			return link;
		}
	}
}
