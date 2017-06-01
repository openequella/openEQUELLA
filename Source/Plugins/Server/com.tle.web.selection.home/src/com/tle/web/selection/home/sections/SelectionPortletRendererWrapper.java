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

package com.tle.web.selection.home.sections;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.component.Box;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class SelectionPortletRendererWrapper extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@Component(register = false)
	private Box box;

	private String portletNameKey;
	private String portletType;

	@Override
	public final SectionResult renderHtml(RenderEventContext context)
	{
		if( !SectionUtils.canViewChildren(context, this) )
		{
			return null;
		}

		NestedRenderable result = (NestedRenderable) SectionUtils.renderSectionResult(context, box);
		result.setNestedRenderable((SectionRenderable) renderFirstResult(context));
		return result;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		tree.registerInnerSection(box, id);

		box.setLabel(getBoxLabel());
		box.setNoMinMaxOnHeader(true);
		box.setStyleClass("portlet_" + portletType + "_content");
	}

	protected Label getBoxLabel()
	{
		return new KeyLabel(portletNameKey);
	}

	public void setPortletNameKey(String portletNameKey)
	{
		this.portletNameKey = portletNameKey;
	}

	public void setPortletType(String portletType)
	{
		this.portletType = portletType;
	}

	public Box getBox()
	{
		return box;
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
