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

package com.tle.web.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.PluginClassLoader;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.GenericNamedResult;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@SuppressWarnings("nls")
@NonNullByDefault
public class PluginFreemarkerFactory extends AbstractFreemarkerFactory implements FreemarkerFactory
{
	private String name = "";
	private URL rootUrl;
	protected String pluginId;
	private PluginClassLoader plugLoader;

	@Inject
	private ResourcesService resourcesService;

	@SuppressWarnings("unused")
	private static Log LOGGER = LogFactory.getLog(PluginFreemarkerFactory.class);

	@Inject
	public void setBeanClassLoader(PluginClassLoader classLoader)
	{
		this.plugLoader = classLoader;
		pluginId = classLoader.getPluginDescriptor().getId();
	}

	@PostConstruct
	public void registerFactory()
	{
		((CustomTemplateLoader) configuration.getTemplateLoader()).addFactory(pluginId + '@' + name, this);
		rootUrl = plugLoader.getResource(getRootPath());
	}

	protected String getRootPath()
	{
		return "view/";
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	@Override
	protected void addRootObjects(Map<String, Object> map, FreemarkerSectionResult result, Writer writer)
	{
		super.addRootObjects(map, result, writer);
		if( writer instanceof SectionWriter )
		{
			SectionWriter sWriter = (SectionWriter) writer;
			SectionId sectionId = result.getSectionId();
			map.put("_info", writer);
			if( sectionId != null )
			{
				String id = sectionId.getSectionId();
				map.put("pfx", id.length() == 0 ? id : id + '.');
				map.put("id", id);
				map.put("s", sWriter.getSectionForId(sectionId));
				if( result.getModel() == null )
				{
					map.put("m", sWriter.getModelForId(id));
				}
			}
			map.put("p", resourcesService.getHelper(pluginId));
		}
	}

	public URL getTemplateResource(String template)
	{
		try
		{
			return new URL(rootUrl, template);
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public NamedSectionResult createNamedResult(String name, String template, SectionId sectionId)
	{
		return new GenericNamedResult(name, createResult(template, sectionId));
	}

	@Override
	public SectionRenderable createResult(String template, SectionId sectionId)
	{
		return createResult(template, sectionId, null);
	}

	@Override
	public SectionRenderable createResult(String template, SectionId sectionId, PreRenderable preRenderer)
	{
		FreemarkerSectionResult result = new FreemarkerSectionResult(this, getTemplate(template));
		result.setSectionId(sectionId);
		result.setPreRenderer(preRenderer);
		return result;
	}

	@Override
	public FreemarkerSectionResult createResultWithModel(String template, Object model)
	{
		FreemarkerSectionResult result = new FreemarkerSectionResult(this, getTemplate(template));
		result.setModel(model);
		return result;
	}

	@Override
	public FreemarkerSectionResult createResultWithModelMap(String template, Object... nameValues)
	{
		Map<Object, Object> nvs = Maps.newHashMap();
		if( nameValues.length % 2 == 1 )
		{
			throw new Error("Need an even number of key/values");
		}
		for( int i = 0; i < nameValues.length; i += 2 )
		{
			nvs.put(nameValues[i], nameValues[i + 1]);
		}

		FreemarkerSectionResult result = new FreemarkerSectionResult(this, getTemplate(template));
		result.setModel(nvs);
		return result;
	}

	@Override
	public TemplateResult createTemplateResult(String template, SectionId sectionId)
	{
		return new FreemarkerTemplateResult(this, (FreemarkerSectionResult) createResult(template, sectionId));
	}

	@Override
	public TemplateResult createTemplateResultWithModel(String template, Object model)
	{
		return new FreemarkerTemplateResult(this, createResultWithModel(template, model));
	}

	private Template getTemplate(String template)
	{
		if( !template.startsWith("/") ) //$NON-NLS-1$
		{
			template = '/' + pluginId + '@' + name + '/' + template;
		}
		try
		{
			return configuration.getTemplate(template);
		}
		catch( IOException e )
		{
			throw new SectionsRuntimeException(e);
		}
	}

	@Override
	protected void finishedRender(Writer writer, FreemarkerSectionResult result, Environment environment)
	{
		if( writer instanceof SectionWriter )
		{
			SectionWriter sWriter = (SectionWriter) writer;
			try
			{
				TemplateModel var = environment.getVariable("PART_READY"); //$NON-NLS-1$
				if( var != null )
				{
					sWriter.addFooterStatements(new ScriptStatement(var.toString()));
				}
				var = environment.getVariable("PART_LOAD"); //$NON-NLS-1$
				if( var != null )
				{
					sWriter.getBody().addEventStatements(JSHandler.EVENT_LOAD, new ScriptStatement(var.toString()));
				}
				var = environment.getVariable("PART_SUBMIT"); //$NON-NLS-1$
				if( var != null )
				{
					sWriter.getBody()
						.addEventStatements(JSHandler.EVENT_PRESUBMIT, new ScriptStatement(var.toString()));
				}
				var = environment.getVariable("PART_HEAD"); //$NON-NLS-1$
				if( var != null )
				{
					sWriter.addHeaderMarkup(var.toString());
				}
				var = environment.getVariable("PART_FUNCTION_DEFINITIONS"); //$NON-NLS-1$
				if( var != null )
				{
					sWriter.addStatements(new ScriptStatement(var.toString()));
				}
				super.finishedRender(sWriter, result, environment);
			}
			catch( TemplateModelException tme )
			{
				throw new SectionsRuntimeException(tme);
			}
		}
	}

	public void setConfiguration(SectionsConfiguration configuration)
	{
		this.configuration = configuration;
	}

}
