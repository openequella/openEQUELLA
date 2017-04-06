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
