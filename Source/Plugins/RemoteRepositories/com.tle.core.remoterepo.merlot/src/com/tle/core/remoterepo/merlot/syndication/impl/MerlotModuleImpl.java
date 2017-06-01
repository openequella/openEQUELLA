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

package com.tle.core.remoterepo.merlot.syndication.impl;

import com.rometools.rome.feed.CopyFrom;
import com.rometools.rome.feed.module.Module;
import com.rometools.rome.feed.module.ModuleImpl;
import com.tle.core.remoterepo.merlot.syndication.MerlotModule;

/**
 * @author aholland
 */
@SuppressWarnings("serial")
public class MerlotModuleImpl extends ModuleImpl implements MerlotModule
{
	private String title;
	private String url;

	protected MerlotModuleImpl()
	{
		super(MerlotModule.class, MerlotModule.URI);
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public void setTitle(String title)
	{
		this.title = title;
	}

	@Override
	public String getUrl()
	{
		return url;
	}

	@Override
	public void setUrl(String url)
	{
		this.url = url;
	}

	@Override
	public void copyFrom(CopyFrom obj)
	{
		MerlotModule other = (MerlotModule) obj;
		title = other.getTitle();
		url = other.getUrl();
	}

	@Override
	public Class<? extends CopyFrom> getInterface()
	{
		return Module.class;
	}
}
