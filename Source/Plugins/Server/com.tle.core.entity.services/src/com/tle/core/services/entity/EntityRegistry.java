package com.tle.core.services.entity;

import java.util.List;

import com.tle.beans.entity.BaseEntity;

public interface EntityRegistry
{
	AbstractEntityService<?, BaseEntity> getServiceForClass(Class<? extends BaseEntity> clazz);

	List<AbstractEntityService<?, BaseEntity>> getAllEntityServices();
}
