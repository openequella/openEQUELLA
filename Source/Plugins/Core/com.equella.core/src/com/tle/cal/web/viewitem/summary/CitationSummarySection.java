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

package com.tle.cal.web.viewitem.summary;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.item.Item;
import com.tle.cal.service.CALService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.summary.section.DisplaySectionConfiguration;
import com.tle.web.viewurl.ItemSectionInfo;


@Bind
public class CitationSummarySection extends AbstractParentViewItemSection<CitationSummarySection.CitaionSummaryModel>
	implements
		DisplaySectionConfiguration
{
	@Inject
	private CALService calService;
	@Inject
	private BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory view;

	private SummarySectionsConfig sectionConfig;


	@Override
	public boolean canView(SectionInfo info)
	{
		return calService.isCopyrightedItem(getItemInfo(info).getItem());
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}
		ItemSectionInfo itemInfo = getItemInfo(context);
		Item item = itemInfo.getItem();

		CALHolding holding = calService.getHoldingForItem(item);
		Map<Long, List<CALPortion>> portions = calService.getPortionsForItems(Collections.singletonList(item));
		CALPortion portion = portions.get(item.getId()) == null ? null : portions.get(item.getId()).get(0);
		String citation = "";
		if(holding != null){
			citation = calService.citate(holding, portion);
		}
		CitaionSummaryModel model = getModel(context);
		model.setCitation((new TextLabel(citation, true)));

		if( sectionConfig != null )
		{
			Label title = new BundleLabel(sectionConfig.getBundleTitle(), bundleCache);
			model.setTitle(title);
		}

		return view.createResult("citationsummary.ftl", this);
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		sectionConfig = config;
	}

	@Override
	public Class<CitaionSummaryModel> getModelClass()
	{
		return CitaionSummaryModel.class;
	}

	public static class CitaionSummaryModel
	{
		Label title;
		Label citation;

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public Label getCitation()
		{
			return citation;
		}

		public void setCitation(Label citation)
		{
			this.citation = citation;
		}

	}
}
