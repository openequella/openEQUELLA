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

package com.tle.web.portal.renderer;

import com.tle.common.portal.entity.Portlet;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

public abstract class PortletContentRenderer<M> extends AbstractPrototypeSection<M>
	implements
		HtmlRenderer,
		ViewableChildInterface
{
	protected Portlet portlet;

	public void setPortlet(Portlet portlet)
	{
		this.portlet = portlet;
		// Dodgy call to prevent #5714
		this.portlet.getAttributes();
	}
}
