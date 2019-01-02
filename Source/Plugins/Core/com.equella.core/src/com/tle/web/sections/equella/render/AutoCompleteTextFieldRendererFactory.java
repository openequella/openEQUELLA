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

package com.tle.web.sections.equella.render;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlTextFieldState;

/**
 * plugin defines the stateClassName as HtmlTextFieldState
 * 
 * @author Andrew Gibb
 */

@Bind
@Singleton
public class AutoCompleteTextFieldRendererFactory implements RendererFactoryExtension
{
	@Override
	public SectionRenderable getRenderer(RendererFactory factory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		return new AutoCompleteTextFieldRenderer((HtmlTextFieldState) state); // NOSONAR
	}
}
