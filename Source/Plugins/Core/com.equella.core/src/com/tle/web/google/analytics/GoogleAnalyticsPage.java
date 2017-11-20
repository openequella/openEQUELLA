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

package com.tle.web.google.analytics;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class GoogleAnalyticsPage extends OneColumnLayout<GoogleAnalyticsPage.GooglePageModel>
{
	public static final String ANALYTICS_KEY = "GOOGLE_ANALYTICS";

	@PlugKey("analytics.pagetitle")
	private static Label TITLE_LABEL;
	@PlugKey("account.receipt")
	private static Label RECEIPT_LABEL;

	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private GoogleAnalyticsPrivilegeTreeProvider securityProvider;

	@EventFactory
	private EventGenerator events;

	@Component(name = "g", stateful = false)
	private TextField accountId;
	@Component
	@PlugKey("account.save")
	private Button save;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		save.setClickHandler(events.getNamedHandler("setup"));
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_BEFORE_EVENTS)
	public void checkAuthorised(SectionInfo info)
	{
		securityProvider.checkAuthorised();
	}

	@Override
	protected TemplateResult getTemplateResult(RenderEventContext info)
	{
		final String acctId = configService.getProperty(ANALYTICS_KEY);
		accountId.setValue(info, acctId);
		getModel(info).setSetup(!Check.isEmpty(acctId));

		GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult("body", viewFactory.createResult("googlepage.ftl", this));
		return templateResult;
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.add(SettingsUtils.getBreadcrumb());
	}

	@EventHandlerMethod
	public void setup(SectionInfo info)
	{
		final String acctId = accountId.getValue(info);
		if( !Check.isEmpty(acctId) )
		{
			configService.setProperty(ANALYTICS_KEY, acctId);
		}
		else
		{
			configService.deleteProperty(ANALYTICS_KEY);
		}

		receiptService.setReceipt(RECEIPT_LABEL);
	}

	public TextField getAccountId()
	{
		return accountId;
	}

	public Button getSave()
	{
		return save;
	}

	@Override
	public Class<GoogleAnalyticsPage.GooglePageModel> getModelClass()
	{
		return GoogleAnalyticsPage.GooglePageModel.class;
	}

	public static class GooglePageModel extends OneColumnLayout.OneColumnLayoutModel
	{
		private boolean setup;

		public boolean isSetup()
		{
			return setup;
		}

		public void setSetup(boolean setup)
		{
			this.setup = setup;
		}
	}
}
