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

package com.tle.web.oaiidentifier;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.settings.standard.OAISettings;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
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
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
public class RootOaiIdentifierSettingsSection extends OneColumnLayout<RootOaiIdentifierSettingsSection.OaiSettingsModel>
{
	@PlugKey("oai.title")
	private static Label TITLE_LABEL;

	@Component(stateful = false)
	private TextField oaiSchemeText;

	@Component(stateful = false)
	private TextField namespaceText;

	@Component(stateful = false)
	private TextField emailText;

	@Component(stateful = false)
	private Checkbox useDownloadItemAcl;

	@PlugKey("oai.settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@EventFactory
	private EventGenerator events;

	@Inject
	private OaiIdentifierSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;

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

		OaiSettingsModel model = getModel(info);
		if( !model.isLoaded() )
		{
			OAISettings oaiSettings = getOAISettings();

			oaiSchemeText.setValue(info, oaiSettings.getScheme());
			namespaceText.setValue(info, oaiSettings.getNamespaceIdentifier());
			emailText.setValue(info, oaiSettings.getEmailAddress());
			useDownloadItemAcl.setChecked(info, oaiSettings.isUseDownloadItemAcl());
			model.setLoaded(true);
		}
		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "oaiidentifier.ftl", this));
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());

	}

	private OAISettings getOAISettings()
	{
		return configService.getProperties(new OAISettings());
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		boolean altered = saveSystemConstants(info);
		if( altered )
		{
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
		}
	}

	private boolean saveSystemConstants(SectionInfo info)
	{
		boolean altered = false;
		OAISettings settings = getOAISettings();

		String oldOaiScheme = settings.getScheme();
		String newOaiScheme = oaiSchemeText.getValue(info);
		if( !(Check.isEmpty(oldOaiScheme) && Check.isEmpty(newOaiScheme)) )
		{
			String old = oldOaiScheme != null ? oldOaiScheme : "";
			if( !old.equals(newOaiScheme) )
			{
				altered |= true;
				settings.setScheme(newOaiScheme);
			}
		}

		String oldNamespace = settings.getNamespaceIdentifier();
		String newNamespace = namespaceText.getValue(info);
		if( !(Check.isEmpty(oldNamespace) && Check.isEmpty(newNamespace)) )
		{
			String old = oldNamespace != null ? oldNamespace : "";
			if( !old.equals(newNamespace) )
			{
				altered |= true;
				settings.setNamespaceIdentifier(newNamespace);
			}
		}

		String oldEmail = settings.getEmailAddress();
		String newEmail = emailText.getValue(info);
		if( !(Check.isEmpty(oldEmail) && Check.isEmpty(newEmail)) )
		{
			String old = oldEmail != null ? oldEmail : "";
			if( !old.equals(newEmail) )
			{
				altered |= true;
				settings.setEmailAddress(newEmail);
			}
		}
		boolean downloadItemAcl = settings.isUseDownloadItemAcl();
		if( !useDownloadItemAcl.isChecked(info) == downloadItemAcl )
		{
			altered |= true;
			settings.setUseDownloadItemAcl(useDownloadItemAcl.isChecked(info));
		}

		// and finally, persist (if any alterations).
		if( altered )
		{
			configService.setProperties(settings);
			OaiSettingsModel model = getModel(info);
			model.setLoaded(false);
		}
		return altered;
	}

	/**
	 * @return the oaiSchemeText
	 */
	public TextField getOaiSchemeText()
	{
		return oaiSchemeText;
	}

	/**
	 * @return the namespaceText
	 */
	public TextField getNamespaceText()
	{
		return namespaceText;
	}

	/**
	 * @return the emailText
	 */
	public TextField getEmailText()
	{
		return emailText;
	}

	public Checkbox getUseDownloadItemAcl()
	{
		return useDownloadItemAcl;
	}

	/**
	 * @return the saveButton
	 */
	public Button getSaveButton()
	{
		return saveButton;
	}

	@Override
	public Class<OaiSettingsModel> getModelClass()
	{
		return OaiSettingsModel.class;
	}

	public static class OaiSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		@Bookmarked(stateful = false)
		private boolean loaded;

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}
	}
}
