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

package com.tle.admin.baseentity;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.EntityPack;

public class EditorState<T extends BaseEntity>
{
	private EntityPack<T> entityPack;
	private boolean readonly;
	private boolean saved;
	private boolean loaded;

	public EntityPack<T> getEntityPack()
	{
		return entityPack;
	}

	public void setEntityPack(EntityPack<T> entityPack)
	{
		this.entityPack = entityPack;
	}

	public T getEntity()
	{
		return entityPack.getEntity();
	}

	public void setEntity(T entity)
	{
		entityPack.setEntity(entity);
	}

	public boolean isReadonly()
	{
		return readonly;
	}

	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

	public boolean isSaved()
	{
		return saved;
	}

	public void setSaved(boolean saved)
	{
		this.saved = saved;
	}

	public boolean isLoaded()
	{
		return loaded;
	}

	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}
}
