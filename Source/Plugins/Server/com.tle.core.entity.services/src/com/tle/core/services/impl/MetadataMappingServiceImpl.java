package com.tle.core.services.impl;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.dytech.edge.ejb.helpers.metadata.mappers.HtmlMapper;
import com.dytech.edge.ejb.helpers.metadata.mappers.PackageMapper;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.itemdef.mapping.HTMLMapping;
import com.tle.beans.entity.itemdef.mapping.Literal;
import com.tle.beans.entity.itemdef.mapping.LiteralMapping;
import com.tle.beans.filesystem.FileHandle;
import com.tle.common.Check;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.MetadataMappingService;

@Bind(MetadataMappingService.class)
@Singleton
@SuppressWarnings("nls")
public class MetadataMappingServiceImpl implements MetadataMappingService
{
	private PluginTracker<PackageMapper> mapperTracker;

	private static Log LOGGER = LogFactory.getLog(MetadataMappingService.class);

	@Inject
	private ScriptingService scriptingService;
	@Inject
	private FileSystemService fileSystemService;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		mapperTracker = new PluginTracker<PackageMapper>(pluginService, "com.tle.web.wizard", "metadatamapper", null);
		mapperTracker.setBeanKey("class");
	}

	@Override
	public void mapPackage(ItemDefinition collection, FileHandle handle, String packageName, PropBagEx itemxml)
	{
		List<PackageMapper> mappers = mapperTracker.getBeanList();
		for( PackageMapper packageMapper : mappers )
		{
			if( packageMapper.isSupportedPackage(handle, packageName) )
			{
				packageMapper.mapMetadata(collection, itemxml, handle, packageName);
			}
		}

	}

	@Override
	public void mapHtmlTags(ItemDefinition collection, FileHandle handle, List<String> filenames, PropBagEx itemxml)
	{
		Collection<HTMLMapping> mappings = collection.getMetadataMapping().getHtmlMapping();
		if( !Check.isEmpty(mappings) )
		{
			Set<String> metaTags = new HashSet<String>();
			for( HTMLMapping htmlMapping : mappings )
			{
				metaTags.add(htmlMapping.getHtml());
			}
			HtmlMapper mapper = new HtmlMapper(metaTags);
			for( String filename : filenames )
			{
				try( InputStream stream = fileSystemService.read(handle, filename) )
				{
					mapper.mapMetaTags(stream);
				}
				catch( Exception e )
				{
					LOGGER.debug("Failed mapping file:" + filename, e);
				}
			}
			Map<String, String> mappedTags = mapper.getMappings();

			for( HTMLMapping htmlMapping : mappings )
			{
				String metaTag = htmlMapping.getHtml();
				if( mappedTags.containsKey(metaTag) )
				{
					String xpath = htmlMapping.getItemdef();
					itemxml.deleteAll(xpath);
					itemxml.setNode(xpath, mappedTags.get(metaTag));
				}
			}

		}
	}

	@Override
	public void mapLiterals(ItemDefinition collection, PropBagEx itemxml, ScriptContext scriptContext)
	{
		Collection<LiteralMapping> mappings = collection.getMetadataMapping().getLiteralMapping();
		if( mappings != null )
		{
			for( LiteralMapping target : mappings )
			{
				String targetValue = target.getValue();
				for( Literal literal : target.getLiterals() )
				{
					String value = literal.getValue();
					String script = literal.getScript();

					if( scriptingService.evaluateScript(script, "metadataMapper", scriptContext) )
					{
						itemxml.setNode(targetValue, value);
					}
				}
			}
		}
	}
}
