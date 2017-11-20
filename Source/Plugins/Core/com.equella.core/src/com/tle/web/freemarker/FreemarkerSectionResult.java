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

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;

import freemarker.template.Template;

public class FreemarkerSectionResult implements SectionRenderable
{
	private final AbstractFreemarkerFactory factory;
	private final Template template;
	private Object model;
	private Map<String, Object> extraObjects;
	private SectionId sectionId;
	private PreRenderable preRenderer;

	public FreemarkerSectionResult(AbstractFreemarkerFactory factory, Template template)
	{
		this.template = template;
		this.factory = factory;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		if( preRenderer != null )
		{
			info.preRender(preRenderer);
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		factory.render(this, writer);
	}

	public AbstractFreemarkerFactory getFactory()
	{
		return factory;
	}

	public SectionId getSectionId()
	{
		return sectionId;
	}

	public void setSectionId(SectionId sectionId)
	{
		this.sectionId = sectionId;
	}

	public Template getTemplate()
	{
		return template;
	}

	public Object getModel()
	{
		return model;
	}

	public void setModel(Object model)
	{
		this.model = model;
	}

	public PreRenderable getPreRenderer()
	{
		return preRenderer;
	}

	public void setPreRenderer(PreRenderable preRenderer)
	{
		this.preRenderer = preRenderer;
	}

	public Map<String, Object> getExtraObjects()
	{
		return extraObjects;
	}

	public void setExtraObjects(Map<String, Object> extraObjects)
	{
		this.extraObjects = extraObjects;
	}

	public void addExtraObject(String key, Object value)
	{
		if( extraObjects == null )
		{
			extraObjects = new HashMap<String, Object>();
		}
		extraObjects.put(key, value);
	}
}
