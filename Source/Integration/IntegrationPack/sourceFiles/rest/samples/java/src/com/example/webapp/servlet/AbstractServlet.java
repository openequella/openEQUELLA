/*
 * Copyright (c) 2011, EQUELLA All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met: Redistributions of source code must
 * retain the above copyright notice, this list of conditions and the following
 * disclaimer. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution. Neither
 * the name of EQUELLA nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.example.webapp.servlet;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.cache.WebappTemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class AbstractServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected static final String KEY_TOKEN = "$token";

	private Template template;

	/**
	 * @return The display template filename (relative to the template folder)
	 */
	protected abstract String getTemplateFilename();

	protected Template getTemplate() throws IOException
	{
		if( template == null || System.getProperties().get("sampleclient.debug") != null )
		{
			template = loadTemplate();
		}
		return template;
	}

	protected Template loadTemplate() throws IOException
	{
		final String templatePath = getTemplateFilename();
		try (InputStreamReader reader = new InputStreamReader(getServletContext().getResourceAsStream(
			"/WEB-INF/template/" + templatePath)))
		{
			final Configuration freemarkerConfig = new Configuration();
			freemarkerConfig.setTemplateLoader(new WebappTemplateLoader(getServletContext(), "/WEB-INF/template"));
			return new Template(templatePath, reader, freemarkerConfig);
		}
	}

	/**
	 * Parses and renders the display template into the response output stream
	 * 
	 * @param response
	 * @param model
	 * @throws IOException
	 */
	protected void renderTemplate(HttpServletResponse response, Object model) throws IOException
	{
		try
		{
			final Environment env = getTemplate().createProcessingEnvironment(model,
				new PrintWriter(response.getOutputStream()));
			env.process();
		}
		catch( TemplateException e )
		{
			throw new IOException(e);
		}
	}

	protected boolean getParameter(HttpServletRequest request, String name, boolean defaultValue)
	{
		String val = request.getParameter(name);
		if( val == null )
		{
			return defaultValue;
		}
		return val.equals("on") || Boolean.valueOf(val);
	}

	protected String getParameter(HttpServletRequest request, String name, String defaultValue)
	{
		String val = request.getParameter(name);
		if( val == null )
		{
			return defaultValue;
		}
		return val;
	}

	protected int getParameter(HttpServletRequest request, String name, int defaultValue)
	{
		String val = request.getParameter(name);
		if( val == null )
		{
			return defaultValue;
		}
		return Integer.valueOf(val);
	}
}
