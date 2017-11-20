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

package com.tle.web.selection;

import java.util.Set;

public class CourseListFolderAjaxUpdateData
{
	private String folderId;
	private Set<String> ajaxIds;
	private String[] event;

	public String getFolderId()
	{
		return folderId;
	}

	public void setFolderId(String folderId)
	{
		this.folderId = folderId;
	}

	public Set<String> getAjaxIds()
	{
		return ajaxIds;
	}

	public void setAjaxIds(Set<String> ajaxIds)
	{
		this.ajaxIds = ajaxIds;
	}

	public String[] getEvent()
	{
		return event;
	}

	public void setEvent(String[] event)
	{
		this.event = event;
	}
}