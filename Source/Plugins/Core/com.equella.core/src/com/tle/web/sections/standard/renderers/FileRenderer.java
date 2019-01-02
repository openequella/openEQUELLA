/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.function.DefaultDisableFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlFileUploadState;

@SuppressWarnings("nls")
public class FileRenderer extends AbstractInputRenderer implements JSDisableable
{
	private static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(FileRenderer.class);

	private static final JSCallable SETUP = new ExternallyDefinedFunction("setupProgression", new IncludeFile(
		urlHelper.url("js/upload.js")));

	protected final HtmlFileUploadState uploadState;
	private int size;
	protected boolean renderBar;
	protected boolean renderFile;

	public FileRenderer(HtmlFileUploadState state)
	{
		super(state, "file");
		this.uploadState = state;
	}

	public FileRenderer(HtmlComponentState state)
	{
		super(state, "file");
		this.uploadState = null;
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);

		if( renderBar )
		{
			// This creates an Iframe which needs the URL to the jquery.js file.
			writer.getBody().addReadyStatements(
				Js.call_s(ProgressRenderer.WEBKIT_PROGRESS_FRAME, urlHelper.url("jquerycore/jquery.js")));
		}

		if( size > 0 )
		{
			attrs.put("size", Integer.toString(size));
		}
	}

	private void renderBar(SectionWriter writer) throws IOException
	{
		writer.writeTag("div", "id", state.getElementId(writer) + "_bar", "class", "progressbar");
		writer.endTag("div");
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		if( renderFile )
		{
			super.realRender(writer);
		}
		if( renderBar )
		{
			renderBar(writer);
		}
	}

	public void setParts(boolean bar, boolean file)
	{
		renderBar = bar;
		renderFile = file;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.getForm().setEncoding("multipart/form-data");

		if( renderBar )
		{
			info.preRender(ProgressRenderer.PRE_RENDERER);

			final JQuerySelector bar = new JQuerySelector(new AppendedElementId(this, "_bar"));
			final JQuerySelector file = new JQuerySelector(this);
			final HeaderHelper header = info.getHelper();
			// This is where it's bad. We're using an ID that the progress
			// servlet doesn't know about yet.
			// The progress servlet only gets to know about it when the upload
			// happens and the TLEMultipartResolver
			// adds a progress session for it.
			final String progressUrl = urlHelper.instUrl("progress/?id=" + getElementId(info));
			final JSStatements setupCall = Js.call_s(SETUP, progressUrl, bar, file);

			info.addReadyStatements(Js.statement(Js.methodCall(Jq.$(header.getFormExpression()), Js.function("submit"),
				Js.function(setupCall))));
		}
	}

	@Override
	public JSCallable createDisableFunction()
	{
		return new DefaultDisableFunction(this);
	}
}
