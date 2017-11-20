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

package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CStaticHTML;

/**
 * @author jmaginnis
 */
@Bind
public class StaticHTML extends AbstractSimpleWebControl
{
	private CStaticHTML staticHtml;

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		staticHtml = (CStaticHTML) control;
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		// nothing
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return new SimpleSectionResult(staticHtml.getResolvedHtml());
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}
