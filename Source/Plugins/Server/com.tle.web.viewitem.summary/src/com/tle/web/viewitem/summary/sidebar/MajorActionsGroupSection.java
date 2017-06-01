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

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
public class MajorActionsGroupSection extends AbstractPrototypeSection<MajorActionsGroupSection.MajorActionsGroupModel>
	implements
		ViewableChildInterface,
		HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}

		MajorActionsGroupModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());
		return view.createResult("viewitem/summary/sidebar/majoractionsgroup.ftl", context);
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return SectionUtils.canViewChildren(info, this);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	public List<SectionId> getChildSectionIds(SectionInfo info)
	{
		return info.getChildIds(this);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new MajorActionsGroupModel();
	}

	public static class MajorActionsGroupModel
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
