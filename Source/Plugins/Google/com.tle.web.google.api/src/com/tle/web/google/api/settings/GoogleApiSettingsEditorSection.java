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

package com.tle.web.google.api.settings;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.google.api.GoogleApiUtils;
import com.tle.web.google.api.privileges.GoogleApiSettingsPrivilegeTreeProvider;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
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

@Bind
public class GoogleApiSettingsEditorSection extends OneColumnLayout<OneColumnLayout.OneColumnLayoutModel>
{
	@PlugKey("settings.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;

	@EventFactory
	private EventGenerator events;

	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private GoogleApiSettingsPrivilegeTreeProvider securityProvider;

	@Component
	private TextField apiKey;

	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@Override
	protected TemplateResult getTemplateResult(RenderEventContext context)
	{
		securityProvider.checkAuthorised();

		final String acctId = configService.getProperty(GoogleApiUtils.GOOGLE_API_KEY);
		apiKey.setValue(context, acctId);

		GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult("body", viewFactory.createResult("googleapi.ftl", this));
		return templateResult;
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		final String key = apiKey.getValue(info).trim();
		if( !Check.isEmpty(key) )
		{
			configService.setProperty(GoogleApiUtils.GOOGLE_API_KEY, key);
		}
		else
		{
			configService.deleteProperty(GoogleApiUtils.GOOGLE_API_KEY);
		}

		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	public TextField getApiKey()
	{
		return apiKey;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}
}
