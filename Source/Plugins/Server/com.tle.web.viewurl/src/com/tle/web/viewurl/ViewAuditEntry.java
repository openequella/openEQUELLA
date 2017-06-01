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

package com.tle.web.viewurl;

public class ViewAuditEntry
{
	private String contentType;
	private String path;
	private boolean summary;

	public ViewAuditEntry(String contentType, String path)
	{
		this.contentType = contentType;
		this.path = path;
		this.summary = false;
	}

	public ViewAuditEntry(boolean summary)
	{
		this.summary = summary;
	}

	public String getPath()
	{
		return path;
	}

	public String getContentType()
	{
		return contentType;
	}

	public boolean isSummary()
	{
		return summary;
	}

	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public void setSummary(boolean summary)
	{
		this.summary = summary;
	}
}
