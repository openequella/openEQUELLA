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

import com.tle.beans.entity.BaseEntity;
import com.tle.core.hibernate.equella.service.InitialiserCallback;
import com.tle.core.hibernate.equella.service.Property;

public class EntityInitialiserCallback implements InitialiserCallback
{
	@Override
	public void set(Object obj, Property property, Object value)
	{
		if( value instanceof BaseEntity )
		{
			BaseEntity toset = (BaseEntity) value;
			toset.setUuid(((BaseEntity) property.get(obj)).getUuid());
		}
		property.set(obj, value);
	}

	@Override
	public void entitySimplified(Object old, Object newObj)
	{
		if( old instanceof BaseEntity )
		{
			BaseEntity toset = (BaseEntity) newObj;
			BaseEntity oldObj = (BaseEntity) old;
			toset.setUuid(oldObj.getUuid());
		}
	}
}
