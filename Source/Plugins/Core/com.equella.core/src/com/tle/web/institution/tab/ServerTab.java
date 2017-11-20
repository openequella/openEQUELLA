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

package com.tle.web.institution.tab;

import java.util.List;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;

@SuppressWarnings("nls")
public class ServerTab extends AbstractInstitutionTab<ServerTab.ServerTabModel>
{
	@PlugKey("institutions.server.settings.name")
	private static Label LINK_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		ServerTabModel model = getModel(context);
		model.setSections(renderChildren(context, new ResultListCollector()).getResultList());
		return viewFactory.createResult("tab/server.ftl", context);
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public Class<ServerTabModel> getModelClass()
	{
		return ServerTabModel.class;
	}

	@Override
	public Label getName()
	{
		return LINK_LABEL;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "servertab";
	}

	public static class ServerTabModel
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
