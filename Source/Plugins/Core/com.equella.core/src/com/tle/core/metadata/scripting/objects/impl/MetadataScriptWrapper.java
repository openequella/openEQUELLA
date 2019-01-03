/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.core.metadata.scripting.objects.impl;

import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.core.guice.Bind;
import com.tle.core.metadata.scripting.objects.MetadataScriptObject;
import com.tle.core.metadata.scripting.types.MetadataScriptType;
import com.tle.core.metadata.scripting.types.impl.MetadataScriptTypeImpl;
import com.tle.core.metadata.service.MetadataService;
import com.tle.core.services.FileSystemService;
import com.tle.web.scripting.objects.AbstractScriptWrapper;
import com.tle.web.scripting.objects.FileScriptingObjectImpl.FileHandleScriptTypeImpl;
import com.tle.web.scripting.types.AttachmentScriptTypeImpl;

import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(MetadataScriptObject.class)
@Singleton
public class MetadataScriptWrapper extends AbstractScriptWrapper implements MetadataScriptObject
{
	@Inject
	private MetadataService metadataService;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public MetadataScriptType getMetadata(FileHandleScriptType f)
	{
		MetadataScriptType mdst = new MetadataScriptTypeImpl(metadataService.getMetadata(fileSystemService
			.getExternalFile(((FileHandleScriptTypeImpl) f).getHandle(), f.getName())));

		return mdst;
	}

	@Override
	public MetadataScriptType getMetadata(AttachmentScriptType a)
	{
		AttachmentScriptTypeImpl impl = (AttachmentScriptTypeImpl) a;
		MetadataScriptType mdst = new MetadataScriptTypeImpl(metadataService.getMetadata(impl.getWrapped(),
			impl.getStagingFile()));

		return mdst;
	}
}
