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

package com.tle.web.institution.tab;

import java.util.Map;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.tle.common.Check;
import com.tle.core.email.EmailService;
import com.tle.core.system.SystemConfigService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.InstitutionSection;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.validators.SimpleValidator;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class EmailsTab extends AbstractPrototypeSection<EmailsTab.EmailsModel> implements HtmlRenderer
{
	@PlugKey("install.noreplyemailsblank")
	private static Label LABEL_EMAILSBLANK;
    @PlugKey("install.emailsblank")
    private static Label LABEL_NOREPLYEMAILSBLANK;
	@PlugKey("install.smtpblank")
	private static Label LABEL_SMTPBLANK;
	@PlugKey("install.invalidemail")
	private static Label LABEL_INVALIDEMAIL;
	@PlugKey("install.smtpuserpassword")
	private static Label LABEL_SMTPUSERPASSWORD;
	@Inject
	private SystemConfigService systemConfigService;

	@Inject
	private EmailService emailService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private InstitutionSection institutionSection;

	@Component
	private TextField emails;
    @Component
    private TextField noReplySender;
	@Component
	private TextField smtpServer;
	@Component
	private TextField smtpUser;
	@Component(stateful = false)
	private TextField smtpPassword;
	@Component(stateful = false)
	private TextField confirmPassword;

	@Component
	@PlugKey(value = "institutions.server.emails.save")
	private Button save;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		SubmitValuesFunction funcExpr = events.getSubmitValuesFunction("saveClicked");
		// Require the passwords match only if the smtp username is supplied. A
		// separate
		// validation will ensure that either all 3 fields a blanks, or all
		// three populated
		SimpleValidator validateCall = new SimpleValidator(new FunctionCallExpression("ensurePasswordMatch", smtpUser,
			smtpPassword.createGetExpression(), confirmPassword.createGetExpression()));
		save.setClickHandler(new OverrideHandler(validateCall, new FunctionCallStatement(funcExpr)));
	}

	@EventHandlerMethod
	public void saveClicked(SectionContext context)
	{
		EmailsModel model = getModel(context);
		String emailsText = emails.getValue(context);
		String smtpText = smtpServer.getValue(context);
		String smtpUserText = smtpUser.getValue(context);
		String smtpPasswordText = smtpPassword.getValue(context);
        String emailsNoReplySender = noReplySender.getValue(context);

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
		if( Check.isEmpty(emailsNoReplySender) )
		{
			model.addError("noreplysender", LABEL_NOREPLYEMAILSBLANK);
		}

        if( Check.isEmpty(smtpText) )
        {
            model.addError("smtp", LABEL_SMTPBLANK);
        }

		if( (!Check.isEmpty(smtpUserText) && Check.isEmpty(smtpPasswordText))
			|| (Check.isEmpty(smtpUserText) && !Check.isEmpty(smtpPasswordText)) )
		{
			model.addError("smtpuser", LABEL_SMTPUSERPASSWORD);
			model.addError("smtppassword", LABEL_SMTPUSERPASSWORD);
		}

		if( model.getErrors().isEmpty() )
		{
			systemConfigService.setEmails(emails.getValue(context));
			systemConfigService.setSmtpServer(smtpServer.getValue(context));
			systemConfigService.setSmtpUser(smtpUser.getValue(context));
			systemConfigService.setSmtpPassword(smtpPassword.getValue(context));
            systemConfigService.setNoReplySender(noReplySender.getValue(context));
		}
		else
		{
			context.preventGET();
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( systemConfigService.adminPasswordNotSet() )
		{
			return null;
		}

		emails.setValue(context, systemConfigService.getEmails());
		smtpServer.setValue(context, systemConfigService.getSmtpServer());
		smtpUser.setValue(context, systemConfigService.getSmtpUser());
		smtpPassword.setValue(context, systemConfigService.getSmtpPassword());
        noReplySender.setValue(context, systemConfigService.getNoReplySender());
		confirmPassword.setValue(context, systemConfigService.getSmtpPassword());
		return viewFactory.createResult("tab/emails.ftl", context);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "email_notification";
	}

	public Button getSave()
	{
		return save;
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

	public TextField getConfirmPassword()
	{
		return confirmPassword;
	}

	@Override
	public Class<EmailsModel> getModelClass()
	{
		return EmailsModel.class;
	}

	public static class EmailsModel
	{
		private final Map<String, Label> errors = Maps.newHashMap();

		public boolean hasError()
		{
			return !errors.isEmpty();
		}

		public void addError(String id, Label label)
		{
			errors.put(id, label);
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}
	}

}
