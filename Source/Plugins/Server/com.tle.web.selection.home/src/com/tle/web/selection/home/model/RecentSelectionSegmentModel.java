/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
