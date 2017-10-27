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

package com.tle.core.entity.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.EntityPack;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;

/**
 * @author aholland
 */
public abstract class EntityEditingSessionImpl<B extends EntityEditingBean, E extends BaseEntity>
	implements
		EntityEditingSession<B, E>
{
	private static final long serialVersionUID = 1L;

	private final String sessionId;
	private String stagingId;
	private final EntityPack<E> pack;
	private final B bean;
	private final Map<String, Object> validationErrors = Maps.newHashMap();
	private final Map<Class<? extends Serializable>, Serializable> attributes = new HashMap<Class<? extends Serializable>, Serializable>();
	private boolean valid;

	public EntityEditingSessionImpl(String sessionId, EntityPack<E> pack, B bean)
	{
		this.sessionId = sessionId;
		this.stagingId = pack.getStagingID();
		this.pack = pack;
		this.bean = bean;
	}

	@Override
	public boolean isNew()
	{
		if( bean != null )
		{
			return bean.getId() == 0;
		}
		return getEntity().getId() == 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends EntityPack<E>> P getPack()
	{
		return (P) pack;
	}

	@Override
	public E getEntity()
	{
		return pack.getEntity();
	}

	@SuppressWarnings("unchecked")
	@Override
	public B getBean()
	{
		return bean;
	}

	@Override
	public String getSessionId()
	{
		return sessionId;
	}

	@Override
	public String getStagingId()
	{
		return stagingId;
	}

	@Override
	public void setStagingId(String stagingId)
	{
		this.stagingId = stagingId;
	}

	@Override
	public Map<String, Object> getValidationErrors()
	{
		return validationErrors;
	}

	@Override
	public Map<Class<? extends Serializable>, Serializable> getAttributes()
	{
		return attributes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T getAttribute(Class<T> key)
	{
		return (T) attributes.get(key);
	}

	@Override
	public <T extends Serializable> void setAttribute(Class<T> key, T value)
	{
		attributes.put(key, value);
	}

	@Override
	public boolean isValid()
	{
		return valid;
	}

	@Override
	public void setValid(boolean valid)
	{
		this.valid = valid;
	}
}
