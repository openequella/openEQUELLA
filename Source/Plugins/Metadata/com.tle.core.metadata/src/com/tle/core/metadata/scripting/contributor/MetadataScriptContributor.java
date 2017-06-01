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

package com.tle.core.metadata.scripting.contributor;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.core.guice.Bind;
import com.tle.core.metadata.scripting.objects.MetadataScriptObject;
import com.tle.core.metadata.scripting.objects.impl.MetadataScriptWrapper;
import com.tle.core.metadata.service.MetadataService;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.core.services.FileSystemService;

@Bind
@Singleton
public class MetadataScriptContributor implements ScriptObjectContributor
{
	@Inject
	private MetadataService metadataService;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{
		objects.put(MetadataScriptObject.DEFAULT_VARIABLE,
			new MetadataScriptWrapper(metadataService, fileSystemService));
	}
}
