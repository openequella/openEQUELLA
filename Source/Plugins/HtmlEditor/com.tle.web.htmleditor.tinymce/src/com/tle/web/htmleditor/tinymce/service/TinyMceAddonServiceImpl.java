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

package com.tle.web.htmleditor.tinymce.service;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.dytech.edge.common.ScriptContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNull;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.htmleditor.beans.HtmlEditorPlugin;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.core.guice.Bind;
import com.tle.core.htmleditor.service.HtmlEditorPluginService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.jackson.mapper.LenientMapperExtension;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.htmleditor.tinymce.TinyMceAddonButtonRenderer;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.TextLabel;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(TinyMceAddonService.class)
@Singleton
public class TinyMceAddonServiceImpl implements TinyMceAddonService
{
	private static final Logger LOGGER = Logger.getLogger(TinyMceAddonService.class);

	@Inject
	private HtmlEditorPluginService htmlEditorPluginService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private BasicFreemarkerFactory custFactory;
	@Inject
	private ScriptingService scriptService;
	@Inject
	private ObjectMapperService objectMapperService;

	/**
	 * TinyMceAddonProvider method
	 */
	@Override
	public List<TinyMceAddOn> getAddons()
	{
		final List<HtmlEditorPlugin> plugins = htmlEditorPluginService.enumerateForType("tinymce");
		final List<TinyMceAddOn> addons = Lists
			.newArrayList(Lists.transform(plugins, new Function<HtmlEditorPlugin, TinyMceAddOn>()
			{
				@Override
				public TinyMceAddOn apply(HtmlEditorPlugin plugin)
				{
					return new HtmlEditorPluginAddon(plugin);
				}
			}));
		return addons;
	}

	public class HtmlEditorPluginAddon implements TinyMceAddOn
	{
		private final String id;
		private final String resourcesUrl;
		private final String baseUrl;
		@Nullable
		private final String clientJs;
		@Nullable
		private final String serverJs;
		// sort-of final
		private List<HtmlEditorButtonDefinition> buttons;
		private ObjectNode config;
		private ObjectNode extra;
		private final ObjectMapper jsonMapper;

		protected HtmlEditorPluginAddon(HtmlEditorPlugin plugin)
		{
			this.id = plugin.getPluginId();
			this.jsonMapper = objectMapperService.createObjectMapper(LenientMapperExtension.NAME);

			// uses PublicFileServlet
			this.resourcesUrl = institutionService.institutionalise(PathUtils.urlPath("public", plugin.getUuid(), "/"));
			// uses HtmlPluginServlet
			this.baseUrl = institutionService
				.institutionalise(PathUtils.urlPath("htmlplugin", Long.toString(plugin.getId()), "plugin") + "/");
			this.clientJs = plugin.getClientJs();
			this.serverJs = plugin.getServerJs();

			final String buttonString = plugin.getButtons();
			if( Check.isEmpty(buttonString) )
			{
				this.buttons = Collections.unmodifiableList(Lists.<HtmlEditorButtonDefinition>newArrayList());
			}
			else
			{
				final List<HtmlEditorButtonDefinition> buttonTemp = Lists.newArrayList();
				try
				{
					final ArrayNode buttonArray = (ArrayNode) jsonMapper.readTree(plugin.getButtons());
					for( Iterator<JsonNode> buttonNodes = buttonArray.elements(); buttonNodes.hasNext(); )
					{
						final ObjectNode button = (ObjectNode) buttonNodes.next();
						final String buttonId = button.get("id").asText();
						final String name = getNode(button, "title", buttonId);
						final String image = getNode(button, "image", null);
						buttonTemp.add(new HtmlEditorButtonDefinition(buttonId, plugin.getPluginId(),
							new TinyMceAddonButtonRenderer(PathUtils.urlPath(baseUrl, image), new TextLabel(name)),
							new TextLabel(name), -1, true));
					}
					this.buttons = Collections.unmodifiableList(buttonTemp);
				}
				catch( IOException io )
				{
					this.buttons = Lists.newArrayList();
					LOGGER.warn("Invalid button configuration", io);
				}
			}

			final String conStr = plugin.getConfig();
			if( !Check.isEmpty(conStr) )
			{
				try
				{
					this.config = (ObjectNode) jsonMapper.readTree(conStr);
				}
				catch( IOException io )
				{
					this.config = jsonMapper.createObjectNode();
					LOGGER.warn("Invalid configuration", io);
				}
			}
			else
			{
				this.config = jsonMapper.createObjectNode();
			}

			final String extraStr = plugin.getExtra();
			if( !Check.isEmpty(extraStr) )
			{
				try
				{
					this.extra = (ObjectNode) jsonMapper.readTree(extraStr);
				}
				catch( IOException io )
				{
					// This should never happen! Extra is determined by our code
					throw Throwables.propagate(io);
				}
			}
			else
			{
				this.extra = jsonMapper.createObjectNode();
			}
		}

		@Nullable
		private String getNode(ObjectNode button, String node, @Nullable String defaultText)
		{
			final JsonNode n = button.get(node);
			if( n != null )
			{
				return n.asText();
			}
			return defaultText;
		}

		@Override
		public void setScriptContext(SectionInfo info, ScriptContext scriptContext)
		{
			info.setAttribute(this, scriptContext);
		}

		private ScriptContext getScriptContext(SectionInfo info)
		{
			return info.getAttribute(this);
		}

		@Override
		public void preRender(PreRenderContext context)
		{
			if( serverJs != null && serverJs.length() > 0 || clientJs != null && clientJs.length() > 0 )
			{
				final ScriptContext scriptContext = getScriptContext(context);
				// server code execution
				if( serverJs != null && serverJs.length() > 0 )
				{
					scriptService.evaluateScript(serverJs, "server.js", scriptContext);
				}
				// client script pre-processing
				if( clientJs != null && clientJs.length() > 0 )
				{
					String client = evaluateMarkUp(context, clientJs, scriptContext);
					context.addFooterStatements(new ScriptStatement(client + "\n"));
				}
			}
		}

		@Override
		public List<HtmlEditorButtonDefinition> getButtons(SectionInfo info)
		{
			return buttons;
		}

		/**
		 * It never applies to the standard mceaction.do section
		 */
		@Override
		public boolean applies(String action)
		{
			return false;
		}

		@Override
		public boolean isEnabled()
		{
			return true;
		}

		@Override
		public SectionResult execute(SectionInfo info, String action, String sessionId, String pageId,
			String tinyMceBaseUrl, boolean restrictedCollectons, Set<String> collectionUuids,
			boolean restrictedDynacolls, Set<String> dynaCollUuids, boolean restrictedSearches, Set<String> searchUuids,
			boolean restrictedContributables, Set<String> contributableUuids)
		{
			// Nothing. Further enhancement: code execution
			return null;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public String getJsUrl()
		{
			final JsonNode minified = extra.get("minified");
			if( minified != null && minified.asBoolean() )
			{
				return PathUtils.urlPath(baseUrl, "editor_plugin.js");
			}
			return PathUtils.urlPath(baseUrl, "editor_plugin_src.js");
		}

		@Override
		@Nullable
		public String getResourcesUrl()
		{
			return resourcesUrl;
		}

		@Override
		public void register(SectionTree tree, String parentId)
		{
			// Nothing
		}

		@Nullable
		@Override
		public ObjectExpression getInitialisation(RenderContext context)
		{
			final ScriptContext scriptContext = getScriptContext(context);
			final ObjectNode cf = jsonMapper.createObjectNode();
			evalObject(config, cf, context, scriptContext);

			return new JsonObjectExpression(cf);
		}

		private void evalObject(ObjectNode source, ObjectNode target, RenderContext context,
			ScriptContext scriptContext)
		{
			final Iterator<Entry<String, JsonNode>> it = source.fields();
			while( it.hasNext() )
			{
				final Entry<String, JsonNode> e = it.next();
				final String name = e.getKey();
				final JsonNode n = e.getValue();
				if( n.isObject() )
				{
					// recursively handle it
					final ObjectNode nt = jsonMapper.createObjectNode();
					target.put(name, nt);
					evalObject((ObjectNode) n, nt, context, scriptContext);
				}
				else if( n.isInt() || n.isLong() )
				{
					target.put(name, n.asInt());
				}
				else if( n.isFloatingPointNumber() || n.isDouble() )
				{
					target.put(name, n.asDouble());
				}
				else if( n.isBoolean() )
				{
					target.put(name, n.asBoolean());
				}
				else if( n.isArray() )
				{
					target.putArray(name).addAll((ArrayNode) n);
				}
				else
				{
					// freemarker eval
					target.put(name, evaluateMarkUp(context, n.asText(), scriptContext));
				}
			}
		}

		@Override
		public String getBaseUrl()
		{
			return baseUrl;
		}
	}

	private String evaluateMarkUp(RenderContext context, String markUp, ScriptContext scriptContext)
	{
		final StringWriter outbuf = new StringWriter();
		try( StringReader rdr = new StringReader(markUp); SectionWriter wtr = new SectionWriter(outbuf, context) )
		{
			final FreemarkerSectionResult inner = custFactory.createResult("config", rdr, context);

			// all the script context objects are available in the freemarker
			// templates
			for( Map.Entry<String, Object> entry : scriptContext.getScriptObjects().entrySet() )
			{
				inner.addExtraObject(entry.getKey(), entry.getValue());
			}

			inner.realRender(wtr);
			return outbuf.toString();
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	public static class JsonObjectExpression extends ObjectExpression
	{
		protected JsonObjectExpression(ObjectNode o)
		{
			super();
			final Iterator<Entry<String, JsonNode>> it = o.fields();
			while( it.hasNext() )
			{
				final Entry<String, JsonNode> e = it.next();
				final String name = e.getKey();
				final JsonNode n = e.getValue();
				if( n.isObject() )
				{
					put(name, new JsonObjectExpression((ObjectNode) n));
				}
				else if( n.isInt() )
				{
					put(name, n.asInt());
				}
				else if( n.isFloatingPointNumber() )
				{
					put(name, n.asDouble());
				}
				else if( n.isBoolean() )
				{
					put(name, n.asBoolean());
				}
				else
				{
					put(name, n.asText());
				}
			}
		}

		@Override
		public void preRender(@NonNull PreRenderContext info)
		{
			// No
		}
	}
}
