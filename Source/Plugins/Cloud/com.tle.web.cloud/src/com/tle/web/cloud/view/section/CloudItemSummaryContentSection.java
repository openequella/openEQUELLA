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

package com.tle.web.cloud.view.section;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.viewitem.summary.ItemSummaryContent;

/**
 * @author Aaron
 */
@Bind
public class CloudItemSummaryContentSection extends AbstractPrototypeSection<Object>
	implements
		ItemSummaryContent,
		HtmlRenderer
{
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ResultListCollector results = new ResultListCollector(true);
		renderChildren(context, results);
		return new DivRenderer("area", results.getFirstResult());
	}
}
