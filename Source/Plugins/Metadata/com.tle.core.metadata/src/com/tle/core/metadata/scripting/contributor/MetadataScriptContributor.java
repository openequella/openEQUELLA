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
