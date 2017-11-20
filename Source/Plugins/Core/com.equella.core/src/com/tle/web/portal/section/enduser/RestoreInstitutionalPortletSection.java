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

package com.tle.web.portal.section.enduser;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.i18n.BundleCache;
import com.tle.core.portal.service.PortletService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.service.PortletWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class RestoreInstitutionalPortletSection
	extends
		AbstractPrototypeSection<RestoreInstitutionalPortletSection.RestoreInstitutionalPortletModel>
	implements
		HtmlRenderer
{
	@Inject
	private PortletService portletService;
	@Inject
	private PortletWebService portletWebService;
	@Inject
	private BundleCache bundleCache;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component
	@PlugKey("enduser.restoreall.label")
	private Button restoreAllButton;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		List<Portlet> portlets = portletService.getViewableButClosedPortlets();
		if( Check.isEmpty(portlets) || CurrentUser.wasAutoLoggedIn() )
		{
			return null;
		}

		getModel(context).setRestorables(
			Lists.newArrayList(Lists.transform(portlets, new Function<Portlet, HtmlLinkState>()
			{
				@Override
				public HtmlLinkState apply(Portlet portlet)
				{
					return new HtmlLinkState(new BundleLabel(portlet.getName(), bundleCache), events.getNamedHandler(
						"restore", portlet.getUuid()));
				}
			})));

		HelpAndScreenOptionsSection.addScreenOptions(context,
			viewFactory.createResult("enduser/restore/screenoptions.ftl", this));
		return null;
	}

	@EventHandlerMethod
	public void restore(SectionInfo info, String portletUuid)
	{
		portletWebService.restore(info, portletUuid);
	}

	@EventHandlerMethod
	public void restoreAll(SectionInfo info)
	{
		portletWebService.restoreAll(info);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "ri";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		restoreAllButton.setClickHandler(events.getNamedHandler("restoreAll"));
	}

	@Override
	public Class<RestoreInstitutionalPortletModel> getModelClass()
	{
		return RestoreInstitutionalPortletModel.class;
	}

	public Button getRestoreAllButton()
	{
		return restoreAllButton;
	}

	public static class RestoreInstitutionalPortletModel
	{
		private List<HtmlLinkState> restorables;

		public List<HtmlLinkState> getRestorables()
		{
			return restorables;
		}

		public void setRestorables(List<HtmlLinkState> restorables)
		{
			this.restorables = restorables;
		}
	}
}
