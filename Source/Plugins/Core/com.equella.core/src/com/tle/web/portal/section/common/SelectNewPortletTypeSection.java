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

package com.tle.web.portal.section.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.tle.common.Pair;
import com.tle.common.portal.PortletTypeDescriptor;
import com.tle.core.portal.service.PortletService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class SelectNewPortletTypeSection
	extends
		AbstractPrototypeSection<SelectNewPortletTypeSection.SelectNewPortletTypeModel> implements HtmlRenderer
{
	@Inject
	private PortletWebService portletWebService;
	@Inject
	private PortletService portletService;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@EventHandlerMethod
	public void typeSelected(SectionInfo info, String type)
	{
		portletWebService.newPortlet(info, type, false);
	}

	@Override
	public Class<SelectNewPortletTypeModel> getModelClass()
	{
		return SelectNewPortletTypeModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "pst";
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		getModel(context).setNoCreatePrivs(!portletWebService.canCreate());
		GenericTemplateResult gtr = new GenericTemplateResult();
		// System account user presumably doesn't need help, and screen-options
		// refers to dashboard
		// customisation which system user doesn't see, instead seeing
		// sysnoportlets.ftl
		if( !CurrentUser.getUserState().isSystem() )
		{
			HelpAndScreenOptionsSection.addHelp(context, view.createResult("common/create/help.ftl", this));
			HelpAndScreenOptionsSection.addScreenOptions(context, renderOptions(context));
		}
		return gtr;
	}

	public static class SelectNewPortletTypeModel
	{
		private boolean noCreatePrivs;
		private List<Pair<HtmlLinkState, String>> portletTypes;

		public List<Pair<HtmlLinkState, String>> getPortletTypes()
		{
			return portletTypes;
		}

		public void setPortletTypes(List<Pair<HtmlLinkState, String>> portletTypes)
		{
			this.portletTypes = portletTypes;
		}

		public boolean isNoCreatePrivs()
		{
			return noCreatePrivs;
		}

		public void setNoCreatePrivs(boolean noCreatePrivs)
		{
			this.noCreatePrivs = noCreatePrivs;
		}
	}

	public static class PortletTypeModel
	{
		private LabelRenderer label;

		public LabelRenderer getLabel()
		{
			return label;
		}

		public void setLabel(LabelRenderer label)
		{
			this.label = label;
		}
	}

	protected boolean isAdmin()
	{
		return false;
	}

	public SectionRenderable renderOptions(RenderContext context)
	{
		if( !portletWebService.canCreate() && !isAdmin() )
		{
			return null;
		}
		else
		{
			final List<Pair<HtmlLinkState, String>> portletTypes = new ArrayList<Pair<HtmlLinkState, String>>();

			for( PortletTypeDescriptor portletType : portletService.listContributableTypes(isAdmin()) )
			{
				final String type = portletType.getType();

				HtmlLinkState link = new HtmlLinkState(new KeyLabel(portletType.getNameKey()), events.getNamedHandler(
					"typeSelected", type));
				portletTypes.add(new Pair<HtmlLinkState, String>(link, portletType.getDescriptionKey()));
			}

			Collections.sort(portletTypes, new Comparator<Pair<HtmlLinkState, String>>()
			{
				@Override
				public int compare(Pair<HtmlLinkState, String> type1, Pair<HtmlLinkState, String> type2)
				{
					return type1.getFirst().getLabelText().compareTo(type2.getFirst().getLabelText());
				}

			});

			final SelectNewPortletTypeModel model = getModel(context);
			model.setPortletTypes(portletTypes);

			return view.createResult("common/create/screenoptions.ftl", this);
		}
	}

}
