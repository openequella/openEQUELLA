package com.tle.core.jackson.impl;

import java.util.List;

import org.java.plugin.registry.Extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ParamFilter;

@NonNullByDefault
@Bind(ObjectMapperService.class)
@Singleton
@SuppressWarnings("nls")
public class ObjectMapperServiceImpl implements ObjectMapperService
{
	@Inject
	private PluginTracker<MapperExtension> mapperTracker;

	@Override
	public ObjectMapper createObjectMapper(String... named)
	{
		ObjectMapper mapper = new ObjectMapper();
		if( named.length != 0 )
		{
			List<Extension> extensions = mapperTracker.getExtensions(new ParamFilter("mapper", named));
			for( Extension mapperExtension : extensions )
			{
				mapperTracker.getBeanByExtension(mapperExtension).extendMapper(mapper);
			}
		}
		return mapper;
	}
}
