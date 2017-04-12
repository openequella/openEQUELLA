package com.tle.core.events;

import com.tle.beans.entity.BaseEntity;
import com.tle.core.events.listeners.ApplicationListener;

/**
 * @author Nicholas Read
 */
public abstract class BaseEntityDeletionEvent<E extends BaseEntity, L extends ApplicationListener>
	extends
		ApplicationEvent<L>
{
	private static final long serialVersionUID = 1L;

	protected final E entity;

	public BaseEntityDeletionEvent(E entity)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.entity = entity;
	}
}
