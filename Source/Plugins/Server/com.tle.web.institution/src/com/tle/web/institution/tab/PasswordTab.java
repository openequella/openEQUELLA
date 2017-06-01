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

import javax.inject.Inject;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.system.SystemConfigService;
import com.tle.exceptions.BadCredentialsException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
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
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class PasswordTab extends AbstractPrototypeSection<PasswordTab.PasswordModel> implements HtmlRenderer
{
	@Inject
	private SystemConfigService systemConfigService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;

	@Component(stateful = false)
	private TextField adminPassword;
	@Component(stateful = false)
	private TextField oldPassword;
	@Component(stateful = false)
	private TextField confirmPassword;
	@Component
	@PlugKey(value = "institutions.password.change", global = true)
	private Button changeButton;

	public static class PasswordModel
	{
		private String error;
		private boolean requiresInitialPassword;
		private boolean changeSuccessful;

		public boolean isRequiresInitialPassword()
		{
			return requiresInitialPassword;
		}

		public void setRequiresInitialPassword(boolean requiresInitialPassword)
		{
			this.requiresInitialPassword = requiresInitialPassword;
		}

		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}

		public void setChangeSuccessful(boolean changeSuccessful)
		{
			this.changeSuccessful = changeSuccessful;
		}

		public boolean isChangeSuccessful()
		{
			return changeSuccessful;
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "password";
	}

	@Override
	public Class<PasswordModel> getModelClass()
	{
		return PasswordModel.class;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setRequiresInitialPassword(systemConfigService.adminPasswordNotSet());
		return viewFactory.createResult("tab/password.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		SubmitValuesFunction funcExpr = events.getSubmitValuesFunction("changePassword");
		SimpleValidator validateCall = new SimpleValidator(new FunctionCallExpression("confirmAction",
			adminPassword.createGetExpression(), confirmPassword.createGetExpression(), oldPassword));
		changeButton.setClickHandler(new OverrideHandler(validateCall, new FunctionCallStatement(funcExpr)));
	}

	@EventHandlerMethod
	public void changePassword(SectionInfo info)
	{
		PasswordModel model = getModel(info);
		boolean success = true;
		String oldPasswordText = oldPassword.getValue(info);
		String newPasswordText = adminPassword.getValue(info);
		if( systemConfigService.adminPasswordNotSet() )
		{
			systemConfigService.setInitialAdminPassword(newPasswordText);
			clearPasswords(info);
		}
		else
		{
			try
			{
				systemConfigService.setAdminPassword(oldPasswordText, newPasswordText);
				clearPasswords(info);
			}
			catch( BadCredentialsException ex )
			{
				model.setError(CurrentLocale.get("institutions.password.incorrect"));
				success = false;

			}
		}
		model.setChangeSuccessful(success);
		info.preventGET();
	}

	public void clearPasswords(SectionInfo info)
	{
		oldPassword.setValue(info, null);
		adminPassword.setValue(info, null);
		confirmPassword.setValue(info, null);
	}

	public TextField getAdminPassword()
	{
		return adminPassword;
	}

	public TextField getOldPassword()
	{
		return oldPassword;
	}

	public TextField getConfirmPassword()
	{
		return confirmPassword;
	}

	public Button getChangeButton()
	{
		return changeButton;
	}
}
