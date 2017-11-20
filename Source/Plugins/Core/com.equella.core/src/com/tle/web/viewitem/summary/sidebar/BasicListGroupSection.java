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

package com.tle.web.viewitem.summary.sidebar;

import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;

@SuppressWarnings("nls")
public class BasicListGroupSection extends AbstractParentViewItemSection<BasicListGroupSection.MinorActionsGroupModel>
{
	private String groupTitleKey;

	public String getGroupTitleKey()
	{
		return groupTitleKey;
	}

	public void setGroupTitleKey(String groupTitleKey)
	{
		this.groupTitleKey = groupTitleKey;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return canViewChildren(info);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}

		MinorActionsGroupModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());
		return viewFactory.createResult("viewitem/summary/sidebar/basiclistgroup.ftl", context);
	}

	@Override
	public Class<MinorActionsGroupModel> getModelClass()
	{
		return MinorActionsGroupModel.class;
	}

	public static class MinorActionsGroupModel
	{
		private List<SectionRenderable> sections;

		public List<SectionRenderable> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}
	}
}
