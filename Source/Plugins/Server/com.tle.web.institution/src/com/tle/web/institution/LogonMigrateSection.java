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

import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import com.google.common.collect.Maps;
import com.tle.beans.DatabaseSchema;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.SystemUserState;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationErrorReport;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.MigrationStatus;
import com.tle.core.migration.SchemaInfo;
import com.tle.core.migration.impl.HibernateMigrationService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.system.SystemConfigService;
import com.tle.exceptions.BadCredentialsException;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.LabelTagRenderer;

@Bind
@SuppressWarnings("nls")
public class LogonMigrateSection extends AbstractPrototypeSection<LogonMigrateSection.Model>
	implements
		HtmlRenderer,
		ParametersEventListener
{
	@PlugKey("logon.password.wrong")
	private static Label LABEL_WRONGPASSWORD;
	@PlugKey("institutions.logon.password")
	private static Label FIELD_LABEL;

	@Inject
	private HibernateMigrationService hibernateMigrationService;
	@Inject
	private MigrationService migrationService;
	@Inject
	private SystemConfigService systemConfigService;
	@Inject
	private UserSessionService userSessionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component(stateful = false)
	private TextField password;
	@Component
	@PlugKey("institutions.logon.login")
	private Button loginButton;
	@Component
	@PlugKey("logon.error.retry")
	private Button retryButton;

	@PlugKey("logon.system.migrate")
	private static Label LABEL_WAITMIGRATE;
	@PlugKey("logon.system.nostatus")
	private static Label LABEL_NOSTATUS;

	private JSCallable waitForStatus;
	private JSCallable waitForMigration;

	@Inject
	private InstallSection installSection;

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(installSection, id);
		loginButton.setClickHandler(events.getNamedHandler("checkPassword"));
		waitForStatus = ajax.getAjaxFunction("waitForStatus");
		waitForMigration = ajax.getAjaxFunction("waitForMigration");
		password.addReadyStatements(new JQueryStatement(password, "focus()"));
		retryButton.setClickHandler(events.getNamedHandler("retrySystem"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		Model model = getModel(context);
		if( !migrationService.isSystemSchemaUp() )
		{
			MigrationStatus status = migrationService.getMigrationsStatus();
			if( status == null )
			{
				return waitForSystem(context, waitForStatus, LABEL_NOSTATUS);
			}
			if( status.getException() != null )
			{
				return renderError(context, new TextLabel(status.getException()));
			}
			UserState userState = CurrentUser.getUserState();
			boolean authenticatedSystem = userState.isAuthenticated() && userState.isSystem();
			if( status.isNeedsInstallation() )
			{
				migrationService.refreshSystemSchema();
				status = migrationService.getMigrationsStatus();
				if( status.isNeedsInstallation() )
				{
					if( !authenticatedSystem )
					{
						setupSystemUser(context);
					}
					return installSection.renderInstall(context);
				}
			}
			if( authenticatedSystem )
			{
				SchemaInfo systemSchema = status.getSystemSchema();
				boolean runningMigrations = systemSchema.getTaskId() != null;
				if( !runningMigrations && systemSchema.isHasErrors() )
				{
					MigrationErrorReport errors = migrationService.getErrorReport(DatabaseSchema.SYSTEM_SCHEMA.getId());
					return renderError(context, new TextLabel(errors.getError()));
				}

				return waitForSystem(context, waitForMigration, LABEL_WAITMIGRATE);
			}
		}
		model.setFieldLabel(new LabelTagRenderer(password, null, FIELD_LABEL));
		return viewFactory.createResult("logon/logon.ftl", this);
	}

	private SectionResult renderError(RenderContext context, Label error)
	{
		getModel(context).setError(error);
		return viewFactory.createResult("logon/error.ftl", this);
	}

	private SectionResult waitForSystem(RenderEventContext context, JSCallable waitCall, Label message)
	{
		context.getBody().addReadyStatements(MigrationJs.WAIT_FOR, waitCall);
		getModel(context).setDescription(message);
		return viewFactory.createResult("logon/waitforsystem.ftl", this);
	}

	@AjaxMethod(priority = SectionEvent.PRIORITY_HIGH)
	public boolean waitForStatus(SectionInfo info)
	{
		MigrationStatus status = migrationService.getMigrationsStatus();
		return status != null;
	}

	@AjaxMethod
	public boolean waitForMigration(SectionInfo info)
	{
		MigrationStatus status = migrationService.getMigrationsStatus();
		SchemaInfo schemaInfo = status.getSystemSchema();
		boolean runningMigrations = schemaInfo.getTaskId() != null;
		if( schemaInfo.getFinishedTaskId() == null && !runningMigrations )
		{
			migrationService.migrateSystemSchema();
			return false;
		}

		return schemaInfo.isHasErrors() || (!runningMigrations);
	}

	private void setupSystemUser(SectionInfo info)
	{
		SystemUserState ustate = new SystemUserState(null);
		ustate.setSessionID(UUID.randomUUID().toString());
		ustate.setIpAddress(info.getRequest().getRemoteAddr());
		ustate.setAuthenticated(true);
		CurrentUser.setUserState(ustate);
		userSessionService.forceSession();
	}

	@EventHandlerMethod(priority = SectionEvent.PRIORITY_HIGH)
	public void retrySystem(SectionInfo info)
	{
		migrationService.refreshSystemSchema();
		MigrationStatus status = migrationService.getMigrationsStatus();
		UserState userState = CurrentUser.getUserState();
		boolean authenticatedSystem = userState.isAuthenticated() && userState.isSystem();
		if( status.getException() == null && authenticatedSystem )
		{
			migrationService.migrateSystemSchema();
		}
	}

	@EventHandlerMethod(priority = SectionEvent.PRIORITY_HIGH, preventXsrf = false)
	public void checkPassword(SectionInfo info)
	{
		try
		{
			String passwordText = password.getValue(info);
			if( !migrationService.isSystemSchemaUp() )
			{
				hibernateMigrationService.checkSystemPassword(passwordText);
			}
			else
			{
				systemConfigService.checkAdminPassword(passwordText);
			}
			setupSystemUser(info);
		}
		catch( BadCredentialsException bce )
		{
			getModel(info).setError(LABEL_WRONGPASSWORD);
			info.preventGET();
		}

	}

	public static class Model
	{
		private Label error;
		private Label description;
		private SectionRenderable fieldLabel;
		private final Map<String, Label> errors = Maps.newHashMap();

		public Label getError()
		{
			return error;
		}

		public void addError(String id, Label label)
		{
			errors.put(id, label);
		}

		public void setError(Label error)
		{
			this.error = error;
		}

		public Label getDescription()
		{
			return description;
		}

		public void setDescription(Label description)
		{
			this.description = description;
		}

		public SectionRenderable getFieldLabel()
		{
			return fieldLabel;
		}

		public void setFieldLabel(SectionRenderable fieldLabel)
		{
			this.fieldLabel = fieldLabel;
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}
	}

	public TextField getPassword()
	{
		return password;
	}

	public boolean shouldShow(SectionInfo info, boolean adminPages)
	{
		UserState currentUser = CurrentUser.getUserState();
		return !migrationService.isSystemSchemaUp() 
			|| (adminPages && (!currentUser.isAuthenticated() || !currentUser.isSystem()));
	}

	public Button getLoginButton()
	{
		return loginButton;
	}

	public Button getRetryButton()
	{
		return retryButton;
	}

	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		String tempPassword = event.getParameter("password", false);
		if( tempPassword != null )
		{
			this.password.setValue(info, tempPassword);
			info.queueEvent(events.getEventHandler("checkPassword").createEvent(info, new String[]{}));
		}
	}

}
