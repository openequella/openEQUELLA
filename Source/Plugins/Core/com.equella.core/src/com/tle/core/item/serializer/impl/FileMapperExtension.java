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

package com.tle.core.item.serializer.impl;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;
import com.tle.web.api.item.interfaces.beans.FileBean;
import com.tle.web.api.item.interfaces.beans.FolderBean;
import com.tle.web.api.item.interfaces.beans.GenericFileBean;
import com.tle.web.api.item.interfaces.beans.RootFolderBean;

@Bind
@Singleton
public class FileMapperExtension implements MapperExtension
{
	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		mapper.registerSubtypes(new NamedType(RootFolderBean.class, RootFolderBean.TYPE));
		mapper.registerSubtypes(new NamedType(FolderBean.class, FolderBean.TYPE));
		mapper.registerSubtypes(new NamedType(FileBean.class, FileBean.TYPE));
		mapper.registerSubtypes(new NamedType(GenericFileBean.class, GenericFileBean.TYPE));
	}
}
