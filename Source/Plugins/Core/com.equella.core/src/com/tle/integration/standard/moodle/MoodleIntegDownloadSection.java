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

package com.tle.integration.standard.moodle;

import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;

@Bind
public class MoodleIntegDownloadSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return viewFactory.createResult("moodledownload.ftl", context); //$NON-NLS-1$
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}
