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

package com.tle.web.htmleditor.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.beans.exception.ValidationError;
import com.tle.common.beans.exception.InvalidDataException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.htmleditor.HtmlEditorConfiguration;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.filesystem.PublicFile;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.htmleditor.HtmlEditorConfigurationEditingSession;
import com.tle.web.htmleditor.HtmlEditorControl;
import com.tle.web.htmleditor.HtmlEditorFactoryInterface;
import com.tle.web.htmleditor.HtmlEditorInterface;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;

/**
 * TODO: anything which edits a HtmlEditorConfiguration is subject to values
 * being over-written by other sessions
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(HtmlEditorService.class)
@Singleton
public class HtmlEditorServiceImpl implements HtmlEditorService
{
	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
	static
	{
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		JSON_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
	}

	private PluginTracker<HtmlEditorFactoryInterface> editorFactories;
	private PluginTracker<HtmlEditorFactoryInterface> controlFactories;
	@Inject
	private ConfigurationService configService;
	@Inject
	private UserSessionService sessionService;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public HtmlEditorControl getControl()
	{
		List<HtmlEditorFactoryInterface> potential = controlFactories.getBeanList();
		if( potential.size() > 0 )
		{
			return potential.get(0).createControl();
		}
		throw new RuntimeException(CurrentLocale.get("com.tle.web.htmleditor.nohtmleditor"));
	}

	@Override
	public HtmlEditorInterface getEditor()
	{
		List<HtmlEditorFactoryInterface> potential = editorFactories.getBeanList();
		if( potential.size() > 0 )
		{
			return potential.get(0).createEditor();
		}
		throw new RuntimeException(CurrentLocale.get("com.tle.web.htmleditor.nohtmleditor"));
	}

	@Override
	public HtmlEditorConfiguration getEditorConfig()
	{
		return configService.getProperties(new HtmlEditorConfiguration());
	}

	@Override
	public HtmlEditorConfigurationEditingSession createEditorConfigEditingSession()
	{
		final HtmlEditorConfiguration config = getEditorConfig().clone();
		final String sessionId = UUID.randomUUID().toString();
		final HtmlEditorConfigurationEditingSession session = new HtmlEditorConfigurationEditingSession();
		session.setConfig(config);
		session.setSessionId(sessionId);
		sessionService.setAttribute(sessionId, session);
		return session;
	}

	@Override
	public HtmlEditorConfigurationEditingSession getEditorConfigEditingSession(String sessionId)
	{
		HtmlEditorConfigurationEditingSession session = sessionService.getAttribute(sessionId);
		if( session == null )
		{
			throw new RuntimeException("Invalid session ID");
		}
		return session;
	}

	@Override
	public void cancelEditorConfigEditingSession(String sessionId)
	{
		sessionService.removeAttribute(sessionId);
	}

	@Override
	public void commitEditorConfigEditingSession(String sessionId)
	{
		configService.setProperties(getEditorConfigEditingSession(sessionId).getConfig());
		sessionService.removeAttribute(sessionId);
	}

	@Override
	public void validateEditorOptions(String editorOptions) throws InvalidDataException
	{
		try
		{
			JSON_MAPPER.readTree(editorOptions);
		}
		catch( IOException io )
		{
			throw new InvalidDataException(new ValidationError("options", io.getMessage()));
		}
	}

	@Override
	public String getStylesheetContents()
	{
		final PublicFile styles = new PublicFile("htmleditorstyles");
		final HtmlEditorConfiguration editorConfig = getEditorConfig();
		final String filename = (Check.isEmpty(editorConfig.getStylesheetUuid()) ? "styles.css" : PathUtils.filePath(
			editorConfig.getStylesheetUuid(), "styles.css"));
		if( fileSystemService.fileExists(styles, filename) )
		{
			try (InputStream in = fileSystemService.read(styles, filename); Reader rdr = new InputStreamReader(in))
			{
				final StringWriter sw = new StringWriter();
				CharStreams.copy(rdr, sw);
				return sw.toString();
			}
			catch( IOException e )
			{
				throw Throwables.propagate(e);
			}
		}
		return "";
	}

	@Override
	public void setStylesheetContents(String css)
	{
		final HtmlEditorConfiguration editorConfig = getEditorConfig();
		final String oldUuid = editorConfig.getStylesheetUuid();
		final PublicFile styles = new PublicFile("htmleditorstyles");
		try
		{
			final String newUuid = UUID.randomUUID().toString();
			final String newFilename = PathUtils.filePath(newUuid, "styles.css");
			fileSystemService.write(styles, newFilename, new StringReader(css), false);
			editorConfig.setStylesheetUuid(newUuid);
			configService.setProperties(editorConfig);

			final String oldFilename = (Check.isEmpty(oldUuid) ? "styles.css" : PathUtils.filePath(oldUuid,
				"styles.css"));
			fileSystemService.removeFile(styles, oldFilename);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Nullable
	@Override
	public String getStylesheetRelativeUrl()
	{
		final PublicFile styles = new PublicFile("htmleditorstyles");
		final HtmlEditorConfiguration editorConfig = getEditorConfig();
		final String uuid = editorConfig.getStylesheetUuid();
		final String filename = (Check.isEmpty(uuid) ? "styles.css" : PathUtils.filePath(uuid, "styles.css"));
		if( fileSystemService.fileExists(styles, filename) )
		{
			return PathUtils.urlPath("public", "htmleditorstyles", uuid, "styles.css");
		}
		return null;
	}

	@Override
	public void exportStylesheet(FileHandle handle, @Nullable String folder)
	{
		final PublicFile styles = new PublicFile("htmleditorstyles");
		if( fileSystemService.fileExists(styles, "styles.css") )
		{
			fileSystemService.copy(styles, "styles.css", handle, PathUtils.filePath(folder, "styles.css"));
		}
	}

	protected void preRender(PreRenderContext context)
	{
		final String pf = getStylesheetRelativeUrl();
		if( pf != null )
		{
			final CssInclude css = CssInclude.include(pf).make();
			css.preRender(context);
		}
	}

	@Override
	public SectionRenderable getHtmlRenderable(RenderContext context, String html)
	{
		return new HtmlOutputRenderable(html);
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		controlFactories = new PluginTracker<HtmlEditorFactoryInterface>(pluginService, "com.tle.web.htmleditor",
			"control", "id", new ExtensionParamComparator("order"));
		controlFactories.setBeanKey("class");

		editorFactories = new PluginTracker<HtmlEditorFactoryInterface>(pluginService, "com.tle.web.htmleditor",
			"editor", "id", new ExtensionParamComparator("order"));
		editorFactories.setBeanKey("class");
	}

	public class HtmlOutputRenderable extends SimpleSectionResult
	{
		public HtmlOutputRenderable(String html)
		{
			super(html);
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			super.preRender(info);
			HtmlEditorServiceImpl.this.preRender(info);
		}
	}
}
