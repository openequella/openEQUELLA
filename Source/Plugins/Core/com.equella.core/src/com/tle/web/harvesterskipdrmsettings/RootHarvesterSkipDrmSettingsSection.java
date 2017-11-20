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

package com.tle.web.harvesterskipdrmsettings;

import javax.inject.Inject;

import com.tle.common.settings.standard.HarvesterSkipDrmSettings;
import com.tle.core.settings.service.ConfigurationService;
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
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
public class RootHarvesterSkipDrmSettingsSection
	extends
		OneColumnLayout<RootHarvesterSkipDrmSettingsSection.HarvesterSkipDrmSettingsModel>
{
	@PlugKey("harvesterskipdrmsettings.title")
	private static Label TITLE_LABEL;

	@Component(name = "sd", parameter = "sdk", supported = true)
	protected Checkbox allowSkip;

	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@PlugKey("harvesterskipdrmsettings.settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;

	@Inject
	private ConfigurationService configService;
	@Inject
	private HarvesterSkipDrmSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private ReceiptService receiptService;
	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();
		allowSkip.setChecked(info, configService.getProperties(new HarvesterSkipDrmSettings()).isHarvestingSkipDrm());
		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "harvesterskipdrmsettings.ftl", this));
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		HarvesterSkipDrmSettings settings = configService.getProperties(new HarvesterSkipDrmSettings());
		boolean oldAllowSkip = settings.isHarvestingSkipDrm();
		boolean newAllowSkip = allowSkip.isChecked(info);
		if( oldAllowSkip != newAllowSkip )
		{
			settings.setHarvestingSkipDrm(newAllowSkip);
			configService.setProperties(settings);
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
		}
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	public Checkbox getAllowSkip()
	{
		return allowSkip;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	@Override
	public Class<HarvesterSkipDrmSettingsModel> getModelClass()
	{
		return HarvesterSkipDrmSettingsModel.class;
	}

	public static class HarvesterSkipDrmSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		// Empty class, if we haven't thought of anything we need to carry from
		// section to template.
	}
}
