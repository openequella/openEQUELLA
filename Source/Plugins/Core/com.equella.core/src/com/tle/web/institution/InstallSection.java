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

package com.tle.web.institution;

import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.core.email.EmailService;
import com.tle.core.guice.Bind;
import com.tle.core.migration.InstallSettings;
import com.tle.core.migration.MigrationService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.EqualityExpression;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class InstallSection extends AbstractPrototypeSection<InstallSection.Model>
{
	@PlugKey("install.passwordblank")
	private static Label LABEL_PASSWORDBLANK;
	@PlugKey("install.licenceblank")
	private static Label LABEL_LICENCEBLANK;
	@PlugKey("install.licenceinvalid")
	private static Label LABEL_BADLICENCE;
	@PlugKey("install.passwordconfirm")
	private static Label LABEL_PASSWORDCONFIRM;
	@PlugKey("install.emailsblank")
	private static Label LABEL_EMAILSBLANK;
    @PlugKey("install.noreplyemailsblank")
    private static Label LABEL_NOREPLYEMAILSBLANK;
	@PlugKey("install.smtpblank")
	private static Label LABEL_SMTPBLANK;
	@PlugKey("install.invalidemail")
	private static Label LABEL_INVALIDEMAIL;
	@PlugKey("install.smtpuserpassword")
	private static Label LABEL_SMTPUSERPASSWORD;

	@Component(stateful = false)
	private TextField emails;
	@Component(stateful = false)
	private TextField smtpServer;
	@Component(stateful = false)
	private TextField smtpUser;
	@Component(stateful = false)
	private TextField smtpPassword;
    @Component(stateful = false)
    private TextField noReplySender;
	@Component(stateful = false)
	private TextField smtpPasswordConfirm;
	@Component(stateful = false)
	private TextField password;
	@Component(stateful = false)
	private TextField passwordConfirm;
	@Component
	@PlugKey("install.install")
	private Button installButton;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private EmailService emailService;

	@Inject
	private MigrationService migrationService;

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		password.setAutocompleteDisabled(true);
		passwordConfirm.setAutocompleteDisabled(true);
		installButton.setClickHandler(events.getNamedHandler("runInstall").addValidator(
			new SimpleValidator(new EqualityExpression(password.createGetExpression(), passwordConfirm
				.createGetExpression())).setFailureStatements(Js.alert_s(LABEL_PASSWORDCONFIRM))));
	}

	public SectionRenderable renderInstall(RenderContext context)
	{
		return viewFactory.createResult("install.ftl", this);
	}

	@EventHandlerMethod
	public void runInstall(SectionInfo info)
	{
		Model model = getModel(info);
		String passwordText = password.getValue(info);
		if( Check.isEmpty(passwordText) )
		{
			model.addError("password", LABEL_PASSWORDBLANK);
		}

		String emailsText = emails.getValue(info);
		if( Check.isEmpty(emailsText) )
		{
			model.addError("emails", LABEL_EMAILSBLANK);
		}

		String[] emailsArray = emailsText.split(";");
		for( String email : emailsArray )
		{
			if( !emailService.isValidAddress(email) )
			{
				model.addError("emails", LABEL_INVALIDEMAIL);
			}
		}

		String smtpText = smtpServer.getValue(info);
		if( Check.isEmpty(smtpText) )
		{
			model.addError("smtp", LABEL_SMTPBLANK);
		}
		
		String mailUsername = smtpUser.getValue(info);
		String mailPassword = smtpPassword.getValue(info);
		if( !Check.isEmpty(mailPassword) )
		{
			if( !mailPassword.equals(smtpPasswordConfirm.getValue(info)) )
			{
				model.addError("smtppass", LABEL_PASSWORDCONFIRM);
			}
			if( Check.isEmpty(mailUsername) )
			{
				model.addError("smtpuser", LABEL_SMTPUSERPASSWORD);
			}
		}
		else if( !Check.isEmpty(mailUsername) )
		{
			model.addError("smtppass", LABEL_SMTPUSERPASSWORD);
		}

        String mailNoReplySender = noReplySender.getValue(info);
        if( Check.isEmpty(mailNoReplySender) )
        {
            model.addError("noreplysender", LABEL_NOREPLYEMAILSBLANK);
        }
		if( model.getErrors().isEmpty() )
		{
			InstallSettings installSettings = new InstallSettings(Hash.hashPassword(passwordText),
				emailsText, smtpText, mailUsername, mailPassword, mailNoReplySender);
			migrationService.setInstallSettings(installSettings);
			migrationService.executeMigrationsForSchemas(Collections.singleton(-1L));
		}
		else
		{
			info.preventGET();
		}
	}

	public static class Model
	{
		private final Map<String, Label> errors = Maps.newHashMap();

		public void addError(String id, Label label)
		{
			errors.put(id, label);
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}
	}

	public Button getInstallButton()
	{
		return installButton;
	}

	public TextField getPassword()
	{
		return password;
	}

	public TextField getPasswordConfirm()
	{
		return passwordConfirm;
	}

	public TextField getEmails()
	{
		return emails;
	}

	public TextField getSmtpServer()
	{
		return smtpServer;
	}

	public TextField getSmtpUser()
	{
		return smtpUser;
	}

	public TextField getSmtpPassword()
	{
		return smtpPassword;
	}

    public TextField getNoReplySender()
    {
        return noReplySender;
    }

	public TextField getSmtpPasswordConfirm()
	{
		return smtpPasswordConfirm;
	}
}
