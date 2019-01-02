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

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSAssignable;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.ElementValueExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PartiallyApply;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.model.HtmlFileDropState;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("nls")
public class FileDropRenderer extends TagRenderer
{
	private static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(FileDropRenderer.class);

	public static final CssInclude CSS = CssInclude.include(urlHelper.url("css/filedrop.css")).make();
	private static final IncludeFile JS = new IncludeFile(urlHelper.url("js/filedrop.js"));

	public static final String KEY_DND = urlHelper.key("dnd.dropfiles");

	private static final JSCallable SETUP = new ExternallyDefinedFunction("setupFileDrop", JS,
		JQueryProgression.PRERENDER);

	protected final HtmlFileDropState dropState;

	public FileDropRenderer(HtmlFileDropState state)
	{
		super("div", state);
		this.dropState = state;
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		addClass(attrs, "filedrop");
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		writer.writeText(CurrentLocale.get(KEY_DND));
		super.writeMiddle(writer);
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		super.writeEnd(writer);

		writer.writeTag("input", "style", "display: none;", "type", "file", "id",
				getElementId(writer)+"_file", "multiple", "true", "tabIndex", "-1");
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.preRender(CSS);
		if (!dropState.isDontInitialise())
		{
			ObjectExpression oe = new ObjectExpression();
			Bookmark ajaxUploadUrl = dropState.getAjaxUploadUrl();
			if (ajaxUploadUrl != null)
			{
				oe.put("ajaxUploadUrl", ajaxUploadUrl.getHref());
				oe.put("validateFile", dropState.getValidateFile());
			}
			else
			{
				throw new SectionsRuntimeException("Must set an ajax upload url for filedrop");
			}
			oe.put("maxFiles", dropState.getMaxFiles());
			final JSStatements initDnd = Js.call_s(SETUP, new ElementByIdExpression(this), oe);
			info.addReadyStatements(initDnd);
		}
	}

	public static JSAssignable setupFileDropFunc(ElementId id)
	{
		return PartiallyApply.partial(SETUP, 1, new ElementByIdExpression(id));
	}
}
