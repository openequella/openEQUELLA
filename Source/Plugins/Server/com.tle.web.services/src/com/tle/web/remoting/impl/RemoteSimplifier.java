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

package com.tle.web.remoting.impl;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.core.hibernate.equella.service.InitialiserCallback;
import com.tle.core.hibernate.equella.service.Property;

public class RemoteSimplifier implements InitialiserCallback
{
	@Override
	public void entitySimplified(Object old, Object newObj)
	{
		if( old instanceof Item )
		{
			Item item = (Item) old;
			Item newItem = (Item) newObj;
			newItem.setId(item.getId());
			newItem.setUuid(item.getUuid());
			newItem.setVersion(item.getVersion());
			newItem.setName(LanguageBundle.clone(item.getName()));
			newItem.setDescription(LanguageBundle.clone(item.getDescription()));
			newItem.setStatus(item.getStatus());
		}
	}

	@Override
	public void set(Object obj, Property property, Object value)
	{
		property.set(obj, value);
	}
}
