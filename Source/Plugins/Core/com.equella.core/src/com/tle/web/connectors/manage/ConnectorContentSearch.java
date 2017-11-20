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

package com.tle.web.connectors.manage;

import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;

public class ConnectorContentSearch
{
	private ExternalContentSortType sort;
	private String course;
	private String folder;
	private boolean archived;
	private boolean reverse;

	public ExternalContentSortType getSort()
	{
		return sort;
	}

	public void setSort(ExternalContentSortType sort)
	{
		this.sort = sort;
	}

	public String getCourse()
	{
		return course;
	}

	public void setCourse(String course)
	{
		this.course = course;
	}

	public boolean isArchived()
	{
		return archived;
	}

	public void setArchived(boolean archived)
	{
		this.archived = archived;
	}

	public String getFolder()
	{
		return folder;
	}

	public void setFolder(String folder)
	{
		this.folder = folder;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}
}
