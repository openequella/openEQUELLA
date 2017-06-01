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
import java.util.HashMap;
import java.util.Map;

import com.dytech.common.io.DevNullWriter;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.TemplateRenderable;
import com.tle.web.sections.render.TemplateResult;

import freemarker.core.Environment;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FreemarkerTemplateResult implements TemplateResult
{
	private Environment environment;
	private final FreemarkerSectionResult freemarkerResult;
	private final Map<String, TemplateRenderable> results = new HashMap<String, TemplateRenderable>();
	private final AbstractFreemarkerFactory factory;

	public FreemarkerTemplateResult(AbstractFreemarkerFactory factory, FreemarkerSectionResult freemarkerResult)
	{
		this.factory = factory;
		this.freemarkerResult = freemarkerResult;
	}

	@Override
	public TemplateRenderable getNamedResult(RenderContext info, String name)
	{
		TemplateRenderable result = results.get(name);
		if( result == null )
		{
			result = new DelayedNamedResult(this, name);
			results.put(name, result);
		}
		return result;
	}

	public Environment getEnvironment(RenderContext info)
	{
		if( environment == null )
		{
			this.environment = factory.render(freemarkerResult, new SectionWriter(new DevNullWriter(), info));
		}
		return environment;
	}

	public static class DelayedNamedResult implements TemplateRenderable
	{
		private final FreemarkerTemplateResult template;
		private final String name;

		public DelayedNamedResult(FreemarkerTemplateResult template, String name)
		{
			this.template = template;
			this.name = name;
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			// nothing
		}

		@Override
		public boolean exists(RenderContext context)
		{
			return getTemplateVariable(context) != null;
		}

		@Override
		public void realRender(SectionWriter writer) throws IOException
		{
			TemplateModel variable = getTemplateVariable(writer);
			if( variable != null )
			{
				writer.write(variable.toString());
			}
		}

		private TemplateModel getTemplateVariable(RenderContext context)
		{
			Environment environment = template.getEnvironment(context);
			try
			{
				return environment.getVariable("TEMP_" + name); //$NON-NLS-1$
			}
			catch( TemplateModelException e )
			{
				throw new SectionsRuntimeException(e);
			}

		}
	}
}
