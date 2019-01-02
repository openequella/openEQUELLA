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

package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.header.HeaderHelper;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ElementByIdExpression extends AbstractExpression implements JSElementExpression
{
	private final ElementId elementId;
	private final String property;

	public ElementByIdExpression(ElementId elementId, String property)
	{
		this.elementId = elementId;
		this.property = property;
		elementId.registerUse();
	}

	public ElementByIdExpression(ElementId elementId)
	{
		this(elementId, ""); //$NON-NLS-1$
	}

	@Override
	public String getExpression(RenderContext info)
	{
		HeaderHelper helper = info.getHelper();
		return JSUtils.getElement(helper.getElementFunction(), elementId.getElementId(info)).getExpression(info)
			+ property;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(info.getHelper().getElementFunction());
	}
}
