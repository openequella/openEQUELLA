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

package com.tle.web.sections.equella.freemarker;

import java.util.List;

import com.tle.common.Check;
import com.tle.web.freemarker.methods.SectionsTemplateModel;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEvent;
import com.tle.web.sections.render.PreRenderOnly;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererSelectable;
import com.tle.web.sections.standard.model.HtmlComponentState;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class ChooseRenderer extends SectionsTemplateModel implements TemplateMethodModelEx
{
	private final RendererFactory factory;

	public ChooseRenderer(RendererFactory factory)
	{
		this.factory = factory;
	}

	@Override
	public Object exec(@SuppressWarnings("rawtypes") List list) throws TemplateModelException
	{
		RenderContext info = getSectionWriter();

		Object wrapped = DeepUnwrap.unwrap((TemplateModel) list.get(0));
		String type = list.get(1).toString();
		SectionRenderable renderable = getSectionRenderable(info, wrapped, type, factory);
		if( renderable == null )
		{
			return TemplateModel.NOTHING;
		}
		return renderable;
	}

	@SuppressWarnings("nls")
	public static SectionRenderable getSectionRenderable(RenderContext context, Object section, String type,
		RendererFactory factory)
	{
		if( section instanceof SectionRenderable )
		{
			return (SectionRenderable) section;
		}
		if( section instanceof PreRenderable )
		{
			return new PreRenderOnly((PreRenderable) section);
		}
		if( section instanceof HtmlComponentState )
		{
			HtmlComponentState state = ((HtmlComponentState) section);
			if( !Check.isEmpty(type) )
			{
				state.setRendererType(context, type);
			}
			SectionRenderable renderer = factory.getRenderer(context, state);
			state.fireRendererCallback(context, renderer);
			return renderer;
		}
		else if( section instanceof SectionId )
		{
			SectionId sectionId = (SectionId) section;
			SectionId sectionObj = context.getSectionForId(sectionId);
			if( sectionObj instanceof RendererSelectable )
			{
				if( !Check.isEmpty(type) )
				{
					((RendererSelectable) sectionObj).setRendererType(context, type);
				}
			}
			ResultListCollector results = new ResultListCollector(true);
			try
			{
				context.processEvent(new RenderEvent(context, sectionId.getSectionId(), results));
			}
			catch( Exception e )
			{
				throw new SectionsRuntimeException("Error", e);
			}
			return results.getFirstResult();
		}
		throw new SectionsRuntimeException("Bad type in section parameter:" + section.getClass()); //$NON-NLS-1$
	}
}
