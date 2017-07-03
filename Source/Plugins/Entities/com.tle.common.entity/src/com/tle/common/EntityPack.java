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
