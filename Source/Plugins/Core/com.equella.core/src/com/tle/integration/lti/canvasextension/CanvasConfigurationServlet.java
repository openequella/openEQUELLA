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

package com.tle.integration.lti.canvasextension;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;
import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.plugins.PluginService;
import com.tle.web.freemarker.CustomTemplateLoader;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.events.StandardRenderContext;
import com.tle.web.sections.generic.DefaultSectionInfo;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class CanvasConfigurationServlet extends HttpServlet
{
	private static final String DEFAULT_WIDTH = "1024";
	private static final String DEFAULT_HEIGHT = "768";

	@Inject
	private InstitutionService institutionService;
	@Inject
	private SectionsController sectionController;
	@Inject
	private CustomTemplateLoader templateLoader;
	@Inject
	private PluginService pluginService;

	@SuppressWarnings("null")
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public void init()
	{
		String factoryId = pluginService.getPluginIdForObject(CanvasConfigurationServlet.class) + '@';
		FreemarkerFactory ff = templateLoader.getFactoryForName(factoryId);
		if( ff == null )
		{
			throw new RuntimeException("No freemarker factory for CanvasConfigurationServlet");
		}
		view = ff;
	}

	@Override
	protected void service(@SuppressWarnings("null") HttpServletRequest req,
		@SuppressWarnings("null") HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setContentType("application/xml");
		resp.setCharacterEncoding("utf-8");
		final CanvasConfigurationModel model = new CanvasConfigurationModel();
		final URL instUrl = institutionService.getInstitutionUrl();
		model.setDomain(instUrl.getHost());
		model.setInstUrl(instUrl.toString());
		// You could render the labels in the FTL in a similar manner
		model.setWidth(initSizeFromParam(req, "width", DEFAULT_WIDTH));
		model.setHeight(initSizeFromParam(req, "height", DEFAULT_HEIGHT));

		final SectionRenderable renderable = view.createResultWithModel("canvasconfig.ftl", model);
		final DefaultSectionInfo info = new DefaultSectionInfo(sectionController);
		info.setRequest(req);
		info.setResponse(resp);

		try( SectionWriter sectionWriter = new SectionWriter(resp.getWriter(), new StandardRenderContext(info)) )
		{
			sectionWriter.preRender(renderable);
			renderable.realRender(sectionWriter);
		}
	}

	@SuppressWarnings("null")
	private String initSizeFromParam(HttpServletRequest req, String paramName, String defaultValue)
	{
		final String sizeParam = req.getParameter(paramName);
		if( sizeParam != null )
		{
			try
			{
				Integer.parseInt(sizeParam);
				return sizeParam.trim();
			}
			catch( NumberFormatException nfe )
			{
				// Ignore
			}
		}
		return defaultValue;
	}

	@NonNullByDefault(false)
	public static class CanvasConfigurationModel
	{
		private String instUrl;
		private String domain;
		private String width;
		private String height;

		public String getInstUrl()
		{
			return instUrl;
		}

		public void setInstUrl(String instUrl)
		{
			this.instUrl = instUrl;
		}

		public String getDomain()
		{
			return domain;
		}

		public void setDomain(String domain)
		{
			this.domain = domain;
		}

		public String getWidth()
		{
			return width;
		}

		public void setWidth(String width)
		{
			this.width = width;
		}

		public String getHeight()
		{
			return height;
		}

		public void setHeight(String height)
		{
			this.height = height;
		}
	}
}
