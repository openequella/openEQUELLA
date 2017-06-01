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

package com.tle.core.metadata.scripting.objects.impl;

import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.core.metadata.scripting.objects.MetadataScriptObject;
import com.tle.core.metadata.scripting.types.MetadataScriptType;
import com.tle.core.metadata.scripting.types.impl.MetadataScriptTypeImpl;
import com.tle.core.metadata.service.MetadataService;
import com.tle.core.services.FileSystemService;
import com.tle.web.scripting.impl.AbstractScriptWrapper;
import com.tle.web.scripting.impl.AttachmentsScriptWrapper.AttachmentScriptTypeImpl;
import com.tle.web.scripting.impl.FileScriptingObjectImpl.FileHandleScriptTypeImpl;

public class MetadataScriptWrapper extends AbstractScriptWrapper implements MetadataScriptObject
{
	private MetadataService metadataService;
	private FileSystemService fileSystemService;

	public MetadataScriptWrapper(MetadataService metadataService, FileSystemService fileSystemService)
	{
		this.metadataService = metadataService;
		this.fileSystemService = fileSystemService;
	}

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
