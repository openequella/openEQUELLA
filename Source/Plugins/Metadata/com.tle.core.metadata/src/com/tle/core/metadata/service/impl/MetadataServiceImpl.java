package com.tle.core.metadata.service.impl;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.core.guice.Bind;
import com.tle.core.metadata.MetadataHandler;
import com.tle.core.metadata.service.MetadataService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;

@Bind(MetadataService.class)
@Singleton
public class MetadataServiceImpl implements MetadataService
{
	@Inject
	private PluginTracker<MetadataHandler> pluginTracker;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public Map<String, Map<String, String>> getMetadata(File f)
	{
		LoadingCache<String, Map<String, String>> metadata = CacheBuilder.newBuilder().build(
			CacheLoader.from(new Function<String, Map<String, String>>()
		{
			@Override
			public Map<String, String> apply(String input)
			{
				return Maps.newHashMap();
			}
		}));
		
		for( MetadataHandler handler : pluginTracker.getBeanList() )
		{
			handler.getMetadata(metadata, f);
		}

		return metadata.asMap();
	}


	@Override
	public Map<String, Map<String, String>> getMetadata(Attachment a, FileHandle handle)
	{
		if( Objects.equal(a.getAttachmentType(), AttachmentType.FILE) )
		{
			return getMetadata(fileSystemService.getExternalFile(handle, a.getUrl()));
		}
		return Maps.newHashMap();
	}
}
