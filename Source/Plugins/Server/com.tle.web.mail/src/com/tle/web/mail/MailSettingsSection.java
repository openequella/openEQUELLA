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

package com.tle.web.mail;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.mail.internet.AddressException;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.settings.standard.MailSettings;
import com.tle.core.email.EmailResult;
import com.tle.core.email.EmailService;
import com.tle.core.encryption.EncryptionService;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.generic.OverrideHandler;
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
@SuppressWarnings("nls")
public class MailSettingsSection extends OneColumnLayout<MailSettingsSection.MailSettingsModel>
{
	@PlugKey("settings.page.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("settings.test.email.subject")
	private static String TEST_EMAIL_SUBJECT;
	@PlugKey("settings.test.email.text")
	private static String TEST_EMAIL_TEXT;
	@PlugKey("settings.test.email.failure")
	private static String TEST_EMAIL_FAILURE;
	@PlugKey("settings.test.email.success")
	private static String TEST_EMAIL_SUCCESS;

	@ResourceHelper
	private PluginResourceHelper RESOURCES;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private MailSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private EmailService emailService;
	@Inject
	private EncryptionService encryptionService;

	@Component(name = "fe", stateful = false)
	private TextField fromEmailAddr;
	@Component(name = "s", stateful = false)
	private TextField serverUrl;
	@Component(name = "dn", stateful = false)
	private TextField displayName;

	@Component(name = "u", stateful = false)
	private TextField username;
	@Component(name = "p", stateful = false)
	private TextField password;

	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@Component(name = "te", stateful = false)
	private TextField testEmailAddr;
	@Component
	@PlugKey("settings.test.button")
	private Button testButton;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		saveButton.setClickHandler(events.getNamedHandler("save"));
		testButton.setClickHandler(
			new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("test"),
				ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), "emailstatus", "required", "testemail")));
	}

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();

		if( !getModel(info).isLoaded() )
		{
			MailSettings mailSettings = getMailSettings();
			fromEmailAddr.setValue(info, mailSettings.getSender());
			username.setValue(info, mailSettings.getUsername());
			// Don't set password in form
			displayName.setValue(info, mailSettings.getSenderName());
			serverUrl.setValue(info, mailSettings.getServer());
			getModel(info).setLoaded(true);
		}

		return new GenericTemplateResult(viewFactory.createNamedResult(BODY, "mailsettings.ftl", this));
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		saveSystemConstants(info);
		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	private MailSettings getMailSettings()
	{
		return configService.getProperties(new MailSettings());
	}

	private void saveSystemConstants(SectionInfo info)
	{
		// Validate
		final MailSettings mailSettings = getMailSettings();

		// Save settings
		mailSettings.setSender(fromEmailAddr.getValue(info));
		mailSettings.setUsername(username.getValue(info));

		// If password is blank... leave unchanged
		String pwd = password.getValue(info);
		if( !Check.isEmpty(pwd) )
		{
			mailSettings.setPassword(encryptionService.encrypt(pwd));
		}
		mailSettings.setSenderName(displayName.getValue(info));
		mailSettings.setServer(serverUrl.getValue(info));

		configService.setProperties(mailSettings);
		getModel(info).setLoaded(false);
	}

	@EventHandlerMethod
	public void test(SectionInfo info)
	{
		MailSettingsModel model = getModel(info);
		MailSettings testSettings = new MailSettings();

		String from = fromEmailAddr.getValue(info);
		String srv = serverUrl.getValue(info);
		String to = testEmailAddr.getValue(info);

		if( Check.isEmpty(to) )
		{
			model.addError("testEmailAddr", CurrentLocale.get(RESOURCES.key("settings.test.validate.to")));
		}
		else
		{
			try
			{
				javax.mail.internet.InternetAddress.parse(to, true);
			}
			catch( AddressException ae )
			{
				model.addError("testEmailAddr", ae.getLocalizedMessage());
			}
		}
		if( Check.isEmpty(from) )
		{
			model.addError("fromEmailAddr", CurrentLocale.get(RESOURCES.key("settings.test.validate.from")));
		}

		if( Check.isEmpty(srv) )
		{
			model.addError("serverUrl", CurrentLocale.get(RESOURCES.key("settings.test.validate.server")));
		}
		else
		{
			try
			{
				javax.mail.internet.InternetAddress.parse(to, true);
			}
			catch( AddressException ae )
			{
				model.addError("serverUrl", ae.getLocalizedMessage());
			}
		}

		if( model.getErrors().size() == 0 )
		{
			testSettings.setSender(from);
			testSettings.setServer(srv);
			testSettings.setUsername(username.getValue(info));

			String origpwd = getMailSettings().getPassword();
			String newpwd = password.getValue(info);
			// Use original if it exists and new is blank
			testSettings.setPassword(Check.isEmpty(newpwd) ? origpwd : newpwd);
			testSettings.setSenderName(displayName.getValue(info));

			Future<EmailResult<String>> result = emailService.sendEmail(CurrentLocale.get(TEST_EMAIL_SUBJECT),
				Lists.newArrayList(to), CurrentLocale.get(TEST_EMAIL_TEXT), testSettings, !Check.isEmpty(newpwd));

			EmailResult<String> emailResult;
			try
			{
				emailResult = result.get();
				boolean successful = emailResult.isSuccessful();
				model.setSuccessful(successful);
				if( !successful )
				{
					Throwable error = emailResult.getError();
					Throwable cause = error.getCause();
					model.addError("emailError", MessageFormat.format("{0} {1}", CurrentLocale.get(TEST_EMAIL_FAILURE),
						(cause != null) ? cause.getMessage() : error.getMessage()));
				}
				else
				{
					model.addError("emailSuccess", CurrentLocale.get(TEST_EMAIL_SUCCESS));
				}
			}
			catch( InterruptedException e )
			{
				model.addError("emailError",
					MessageFormat.format("{0} {1}", CurrentLocale.get(TEST_EMAIL_FAILURE), e.getMessage()));
			}
			catch( ExecutionException e )
			{
				model.addError("emailError",
					MessageFormat.format("{0} {1}", CurrentLocale.get(TEST_EMAIL_FAILURE), e.getMessage()));
			}
		}
	}

	@Override
	public Class<MailSettingsModel> getModelClass()
	{
		return MailSettingsModel.class;
	}

	public static class MailSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		@Bookmarked
		private boolean loaded;
		private boolean successful;

		private Map<String, String> errors = new HashMap<String, String>();

		public Map<String, String> getErrors()
		{
			return errors;
		}

		public void setErrors(Map<String, String> errors)
		{
			this.errors = errors;
		}

		public void addError(String key, String value)
		{
			this.errors.put(key, value);
		}

		public boolean isSuccessful()
		{
			return successful;
		}

		public void setSuccessful(boolean successful)
		{
			this.successful = successful;
		}

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}
	}

	public TextField getFromEmailAddr()
	{
		return fromEmailAddr;
	}

	public TextField getPassword()
	{
		return password;
	}

	public TextField getDisplayName()
	{
		return displayName;
	}

	public TextField getServerUrl()
	{
		return serverUrl;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public TextField getUsername()
	{
		return username;
	}

	public Button getTestButton()
	{
		return testButton;
	}

	public TextField getTestEmailAddr()
	{
		return testEmailAddr;
	}
}
