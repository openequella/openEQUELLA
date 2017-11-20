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

package com.tle.web.cloud.search.section;

import com.tle.web.cloud.event.CloudSearchEvent;
import com.tle.web.search.filter.SimpleResetFiltersQuerySection;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;

@SuppressWarnings("nls")
public class CloudQuerySection extends SimpleResetFiltersQuerySection<CloudSearchEvent>
{
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		renderQueryActions(context, getModel(context));
		return viewFactory.createResult("cloudquery.ftl", this);
	}
}
