/*
 * Created on Oct 26, 2005
 */
package com.tle.common;

import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.beans.entity.BaseEntity;

public class EntityPack<T extends BaseEntity> extends ImportExportPack<T>
{
	private static final long serialVersionUID = 1L;

	private String stagingID;
	private final Map<String, Object> attributes = Maps.newHashMap();

	public EntityPack()
	{
		super();
	}

	public EntityPack(T entity, String stagingID)
	{
		setEntity(entity);
		this.stagingID = stagingID;
	}

	public String getStagingID()
	{
		return stagingID;
	}

	public void setStagingID(String stagingID)
	{
		this.stagingID = stagingID;
	}

	public void setAttribute(String name, Object value)
	{
		attributes.put(name, value);
	}

	@SuppressWarnings("unchecked")
	public <O> O getAttribute(String name)
	{
		return (O) attributes.get(name);
	}
}
