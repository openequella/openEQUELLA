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

package com.tle.web.freemarker.methods;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.StyleableRenderer;

import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

@NonNullByDefault
public abstract class AbstractRenderDirective extends SectionsTemplateModel implements TemplateDirectiveModel
{
	private static Method currentContextMethod;
	@Nullable
	private static Field bodyField;

	static
	{
		try
		{
			currentContextMethod = Environment.class.getDeclaredMethod("getCurrentMacroContext"); //$NON-NLS-1$
			currentContextMethod.setAccessible(true);
		}
		catch( Exception e )
		{
			throw new SectionsRuntimeException(e);
		}
	}

	@NonNullByDefault(false)
	@SuppressWarnings({"unchecked", "nls", "rawtypes"})
	@Override
	public void execute(Environment env, Map params, TemplateModel[] arg2, TemplateDirectiveBody body)
		throws TemplateException, IOException
	{
		RenderContext info = getSectionWriter();
		try
		{
			Object model = params.get("section"); //$NON-NLS-1$
			if( model instanceof AdapterTemplateModel )
			{
				Object wrapped = ((AdapterTemplateModel) model).getAdaptedObject(Object.class);
				if( wrapped != null )
				{
					SectionRenderable renderable = getRenderable(wrapped, params);
					if( renderable == null )
					{
						return;
					}
					if( renderable instanceof StyleableRenderer )
					{
						((StyleableRenderer) renderable).setStyles(getParam("style", params),
							getParam("class", params), getParam("id", params));
					}
					if( renderable instanceof NestedRenderable && body != null && getBodyField(env) != null )
					{
						((NestedRenderable) renderable).setNestedRenderable(new BodyDirectiveRenderable(body));
					}
					SectionWriter writer = new SectionWriter(env.getOut(), info);
					writer.preRender(renderable);
					renderable.realRender(writer);
				}
			}
			else
			{
				throw new RuntimeException("'section' parameter to render macro was not of the appropriate type (was "
					+ model.getClass() + ").  Perhaps you named your section property the same as a method name?");
			}
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	public static class BodyDirectiveRenderable implements SectionRenderable
	{

		private final TemplateDirectiveBody body;

		public BodyDirectiveRenderable(TemplateDirectiveBody body)
		{
			this.body = body;
		}

		@Override
		public void realRender(SectionWriter writer) throws IOException
		{
			try
			{

				body.render(writer);
			}
			catch( TemplateException e )
			{
				throw new SectionsRuntimeException(e);
			}
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			// nothing
		}

	}

	@Nullable
	protected String getParam(String param, Map<?, ?> params)
	{
		Object val = params.get(param);
		if( val != null )
		{
			return val.toString();
		}
		return null;
	}

	@Nullable
	protected abstract SectionRenderable getRenderable(Object section, Map<String, TemplateModel> params);

	@Nullable
	private static synchronized Object getBodyField(Environment env) throws Exception
	{
		Object context = currentContextMethod.invoke(env);
		if( bodyField == null )
		{
			bodyField = context.getClass().getDeclaredField("nestedContent");
			bodyField.setAccessible(true);
		}
		return bodyField.get(context);
	}

}