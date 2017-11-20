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

package com.tle.core.system.impl;

import java.io.Serializable;

import com.tle.core.hibernate.dao.GenericDaoImpl;

@SuppressWarnings("nls")
public abstract class AbstractSystemDaoImpl<T, ID extends Serializable> extends GenericDaoImpl<T, ID>
{
	public AbstractSystemDaoImpl(Class<T> persistentClass)
	{
		super(persistentClass);
	}

	@Override
	protected final String getFactoryName()
	{
		return "system";
	}

	@Override
	protected final boolean isSystemDataSource()
	{
		return true;
	}
}
