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
