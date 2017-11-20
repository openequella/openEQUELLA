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

package com.tle.mypages.web.section;

import com.tle.mypages.MyPagesConstants;
import com.tle.mypages.web.model.RootMyPagesModel;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.ResultListCollector;

/**
 * @author aholland
 */
public class RootMyPagesSection extends AbstractMyPagesSection<RootMyPagesModel> implements HtmlRenderer
{
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		RootMyPagesModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());

		return viewFactory.createResult("mypagesmodal.ftl", context);
	}

	@Override
	public Class<RootMyPagesModel> getModelClass()
	{
		return RootMyPagesModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return MyPagesConstants.SECTION_ROOT;
	}
}
