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
import com.rometools.rome.feed.module.ModuleImpl;
import com.tle.core.remoterepo.merlot.syndication.MerlotTopLevelModule;

/**
 * @author aholland
 */
@SuppressWarnings("serial")
public class MerlotTopLevelModuleImpl extends ModuleImpl implements MerlotTopLevelModule
{
	private int totalCount;
	private int resultCount;
	private int lastRecNumber;

	protected MerlotTopLevelModuleImpl()
	{
		super(MerlotTopLevelModule.class, MerlotTopLevelModule.URI);
	}

	@Override
	public void copyFrom(CopyFrom obj) {
		MerlotTopLevelModule other = (MerlotTopLevelModule) obj;
		totalCount = other.getTotalCount();
		resultCount = other.getResultCount();
		lastRecNumber = other.getLastRecNumber();		
	}

	@Override
	public Class<MerlotTopLevelModule> getInterface()
	{
		return MerlotTopLevelModule.class;
	}

	@Override
	public int getTotalCount()
	{
		return totalCount;
	}

	@Override
	public void setTotalCount(int totalCount)
	{
		this.totalCount = totalCount;
	}

	@Override
	public int getResultCount()
	{
		return resultCount;
	}

	@Override
	public void setResultCount(int resultCount)
	{
		this.resultCount = resultCount;
	}

	@Override
	public int getLastRecNumber()
	{
		return lastRecNumber;
	}

	@Override
	public void setLastRecNumber(int lastRecNumber)
	{
		this.lastRecNumber = lastRecNumber;
	}

}
