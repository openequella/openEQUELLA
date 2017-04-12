package com.tle.core.remoterepo.merlot.syndication.impl;

import com.sun.syndication.feed.module.ModuleImpl;
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
	public void copyFrom(Object obj)
	{
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
