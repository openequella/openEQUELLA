package com.tle.core.entity.registry;

import java.util.List;

import com.tle.beans.entity.BaseEntity;
import com.tle.core.entity.service.AbstractEntityService;

public interface EntityRegistry
{
	AbstractEntityService<?, BaseEntity> getServiceForClass(Class<? extends BaseEntity> clazz);

	List<AbstractEntityService<?, BaseEntity>> getAllEntityServices();
}
