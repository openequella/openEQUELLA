package com.tle.common;

import java.io.Serializable;
import java.util.Map;

import com.tle.common.security.TargetList;

public class ImportExportPack<T extends Object> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private T entity;
	private String version;
	private TargetList targetList;
	private Map<Object, TargetList> otherTargetLists;

	public T getEntity()
	{
		return entity;
	}

	public void setEntity(T entity)
	{
		this.entity = entity;
	}

	public Map<Object, TargetList> getOtherTargetLists()
	{
		return otherTargetLists;
	}

	public void setOtherTargetLists(Map<Object, TargetList> otherTargetLists)
	{
		this.otherTargetLists = otherTargetLists;
	}

	public TargetList getTargetList()
	{
		return targetList;
	}

	public void setTargetList(TargetList targetList)
	{
		this.targetList = targetList;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}
}
