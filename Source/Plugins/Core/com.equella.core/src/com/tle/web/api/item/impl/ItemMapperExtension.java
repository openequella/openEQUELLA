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

package com.tle.web.api.item.impl;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ItemMapperExtension implements MapperExtension
{
	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		mapper.registerModule(new ItemMapperExtensionModule());
	}

	public static class ItemMapperExtensionModule extends SimpleModule
	{
		public ItemMapperExtensionModule()
		{
			super("ItemMapperExtensionModule");
			registerSubtypes(new NamedType(EquellaItemBean.class, "equellaItem"));
		}
	}
}
