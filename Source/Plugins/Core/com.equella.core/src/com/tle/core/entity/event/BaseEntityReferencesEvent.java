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
