package com.tle.core.entity.event;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.BaseEntity;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.listeners.ApplicationListener;

@NonNullByDefault
public abstract class BaseEntityReferencesEvent<E extends BaseEntity, L extends ApplicationListener>
	extends
		ApplicationEvent<L>
{
	private static final long serialVersionUID = 1L;

	protected final E entity;
	protected final List<Class<?>> referencingClasses = Lists.newArrayList();

	public BaseEntityReferencesEvent(E entity)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.entity = entity;
	}

	public List<Class<?>> getReferencingClasses()
	{
		return referencingClasses;
	}
}
