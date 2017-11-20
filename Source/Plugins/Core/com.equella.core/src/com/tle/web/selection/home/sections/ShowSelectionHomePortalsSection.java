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

package com.tle.web.selection.home.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.NumberOrder;
import com.tle.web.sections.generic.NumberOrderComparator;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;

public class ShowSelectionHomePortalsSection extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	private Pair<List<SectionId>, List<SectionId>> portletLayout;
	private List<SectionId> dymanicPortals;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final Pair<List<SectionId>, List<SectionId>> layout = getPortalLayout(context);

		final GenericTemplateResult gtr = new GenericTemplateResult();

		int lCount = SectionUtils.countViewable(context, layout.getFirst());
		int rCount = SectionUtils.countViewable(context, layout.getSecond());

		List<SectionId> left = new ArrayList<SectionId>(layout.getFirst());
		List<SectionId> right = new ArrayList<SectionId>(layout.getSecond());

		if( lCount <= rCount )
		{
			left.addAll(dymanicPortals);
		}
		else
		{
			right.addAll(dymanicPortals);
		}

		gtr.addNamedResult(TwoColumnLayout.LEFT, SectionUtils.renderSectionsCombined(context, left));
		gtr.addNamedResult(TwoColumnLayout.RIGHT, SectionUtils.renderSectionsCombined(context, right));

		return gtr;
	}

	private synchronized Pair<List<SectionId>, List<SectionId>> getPortalLayout(RenderEventContext context)
	{
		if( portletLayout == null )
		{
			List<LayoutData> left = new ArrayList<LayoutData>();
			List<LayoutData> right = new ArrayList<LayoutData>();
			List<LayoutData> dynamic = new ArrayList<LayoutData>();

			List<SectionId> childIds = context.getChildIds(this);
			for( SectionId childId : childIds )
			{
				LayoutData data = new LayoutData();
				String idStr = childId.getSectionId();
				data.setSectionId(childId);

				String layout = context.getLayout(idStr);
				try
				{
					JSONObject json = JSONObject.fromObject(layout);
					data.setOrder(json.getInt("order")); //$NON-NLS-1$

					if( json.get("column").equals("dynamic") ) //$NON-NLS-1$//$NON-NLS-2$
					{
						dynamic.add(data);
					}
					else
					{
						(json.get("column").equals("left") ? left : right).add(data); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				catch( RuntimeException ex )
				{
					throw new SectionsRuntimeException("The layout property for " + childId
						+ " is missing or incorrect.  Must be in JSON format with properties"
						+ " \"{order: 1234, column: 'left'|'right'}\", but it was \"" + layout + "\"", ex);
				}
			}

			Collections.sort(left, NumberOrderComparator.LOWEST_FIRST);
			Collections.sort(right, NumberOrderComparator.LOWEST_FIRST);

			portletLayout = new Pair<List<SectionId>, List<SectionId>>(Lists.transform(left, DATA_TO_SECTION_ID),
				Lists.transform(right, DATA_TO_SECTION_ID));

			dymanicPortals = Lists.transform(dynamic, DATA_TO_SECTION_ID);
		}

		return portletLayout;
	}

	private static final Function<LayoutData, SectionId> DATA_TO_SECTION_ID = new Function<LayoutData, SectionId>()
	{
		@Override
		public SectionId apply(LayoutData data)
		{
			return data.getSectionId();
		}
	};

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	public static class LayoutData implements NumberOrder
	{
		private int order;
		private SectionId sectionId;

		@Override
		public int getOrder()
		{
			return order;
		}

		public void setOrder(int order)
		{
			this.order = order;
		}

		public SectionId getSectionId()
		{
			return sectionId;
		}

		public void setSectionId(SectionId sectionId)
		{
			this.sectionId = sectionId;
		}
	}
}