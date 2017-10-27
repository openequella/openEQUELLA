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

package com.tle.core.item.service;

import java.io.Serializable;

import com.tle.beans.Institution;

/**
 * @author jmaginnis
 */
public class DownloadMessage implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String url;
	private long itemdef;
	private final String userid;
	private final Institution institution;

	public DownloadMessage(String userid, Institution institution)
	{
		super();
		this.userid = userid;
		this.institution = institution;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public long getItemdef()
	{
		return itemdef;
	}

	public void setItemdef(long itemdef)
	{
		this.itemdef = itemdef;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public String getUserid()
	{
		return userid;
	}

}
