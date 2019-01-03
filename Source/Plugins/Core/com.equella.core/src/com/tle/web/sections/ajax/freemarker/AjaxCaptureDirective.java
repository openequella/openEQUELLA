/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.ajax.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.tle.web.freemarker.methods.SectionsTemplateModel;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;

import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;

public class AjaxCaptureDirective extends SectionsTemplateModel implements TemplateDirectiveModel
{
	@SuppressWarnings({"nls"})
	@Override
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map params, TemplateModel[] loopVars,
		TemplateDirectiveBody body) throws TemplateException, IOException
	{
		SectionWriter context = getSectionWriter();
		AjaxRenderContext ajaxContext = context.getAttribute(AjaxRenderContext.class);
		Writer out = env.getOut();
		TagRenderer renderer;
		AdapterTemplateModel tagModel = (AdapterTemplateModel) params.get("tag");
		Object tagObject = tagModel == null ? null : tagModel.getAdaptedObject(Object.class);
		if( tagObject == null )
		{
			renderer = new TagRenderer("div", new TagState());
			Object idParam = params.get("id");
			if( idParam == null )
			{
				throw new IllegalArgumentException("Must supply \"id\" or \"tag\" attribute");
			}
		}
		else if( tagObject instanceof TagRenderer )
		{
			renderer = (TagRenderer) tagObject;
		}
		else
		{
			renderer = new TagRenderer("div", (TagState) tagObject);
		}

		Object idParam = params.get("id");
		if( idParam != null )
		{
			renderer.setId(idParam.toString());
		}
		renderer.registerUse();
		String idString = renderer.getElementId(context);
		Object classParam = params.get("class");
		String classString = (classParam != null ? classParam.toString() : null);
		Object collectionObj = params.get("collection");
		boolean writediv;
		boolean collection = collectionObj != null && ((TemplateBooleanModel) collectionObj).getAsBoolean();
		Map<String, Object> divParams = new HashMap<String, Object>();

		@SuppressWarnings("unchecked")
		Set<Map.Entry<?, ?>> entrySet = params.entrySet();
		for( Map.Entry<?, ?> entry : entrySet )
		{
			String param = entry.getKey().toString();
			Object paramVal = entry.getValue();
			if( param.startsWith("param_") && paramVal != null )
			{
				divParams.put(param.substring(6), convertToJava(paramVal));
			}
		}

		Writer contentsOut = out;
		if( ajaxContext != null )
		{
			contentsOut = ajaxContext.startCapture(out, idString, divParams, collection);
			if( !out.equals(contentsOut) )
			{
				addEffectLibrary(ajaxContext, divParams, "showEffect");
				addEffectLibrary(ajaxContext, divParams, "hideEffect");
			}
		}

		Object writedivObj = params.get("writediv");
		writediv = writedivObj == null || ((TemplateBooleanModel) writedivObj).getAsBoolean();
		if( writediv )
		{
			renderer.setStyles("", classString, idString);
			renderer.setNestedRenderable(new BodyRenderer(body, contentsOut));
			context.preRender(renderer);
			renderer.realRender(new SectionWriter(out, context));
		}
		else
		{
			body.render(contentsOut);
		}
		if( ajaxContext != null )
		{
			ajaxContext.endCapture(idString);
		}
	}

	private Object convertToJava(Object paramVal) throws TemplateModelException
	{
		if( paramVal instanceof TemplateNumberModel )
		{
			return ((TemplateNumberModel) paramVal).getAsNumber();
		}
		return paramVal.toString();
	}

	private void addEffectLibrary(AjaxRenderContext context, Map<String, Object> params, String effect)
	{
		Object effectName = params.get(effect);
		if( effectName instanceof String )
		{
			context.preRender(JQueryUIEffects.getEffectLibrary((String) effectName));
		}
	}

	public static class BodyRenderer implements SectionRenderable
	{

		private final TemplateDirectiveBody body;
		private final Writer bodyOut;

		public BodyRenderer(TemplateDirectiveBody body, Writer writer)
		{
			this.body = body;
			this.bodyOut = writer;
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			// nothing
		}

		@Override
		public void realRender(SectionWriter writer) throws IOException
		{
			try
			{
				body.render(bodyOut);
			}
			catch( TemplateException e )
			{
				throw new SectionsRuntimeException(e);
			}
		}
	}
}
