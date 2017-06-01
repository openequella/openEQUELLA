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

package com.tle.web.viewitem.summary.section;

import javax.inject.Inject;

import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.service.ItemXsltService;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class XsltSection extends AbstractParentViewItemSection<Object> implements DisplaySectionConfiguration
{
	// @Inject
	// private HtmlEditorService htmlEditorService;
	@Inject
	private ItemXsltService itemXslService;
	private String config;

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( Check.isEmpty(config) )
		{
			return null;
		}

		ItemSectionInfo iinfo = getItemInfo(context);
		// TODO: XSLT should *really* allow for HTML output, but it doesn't
		// return new DivRenderer(HtmlEditorService.DISPLAY_CLASS,
		// htmlEditorService.getHtmlRenderable(context,
		// itemXslService.renderSimpleXsltResult(context, iinfo, config)));
		return new SimpleSectionResult(itemXslService.renderSimpleXsltResult(context, iinfo, config));
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "xslt"; //$NON-NLS-1$
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		this.config = config.getConfiguration();
	}
}
