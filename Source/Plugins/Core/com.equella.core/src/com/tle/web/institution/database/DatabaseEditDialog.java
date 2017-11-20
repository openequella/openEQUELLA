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

package com.tle.web.institution.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.DatabaseSchema;
import com.tle.common.Check;
import com.tle.common.beans.exception.ValidationError;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.core.migration.MigrationService;
import com.tle.hibernate.dialect.ExtendedPostgresDialect;
import com.tle.hibernate.dialect.SQLServerDialect;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.js.impl.CombinedDisableable;

@Bind
@NonNullByDefault
@SuppressWarnings("nls")
public class DatabaseEditDialog extends AbstractOkayableDialog<DatabaseEditDialog.Model>
{
	@PlugKey("databases.dialog.title.add")
	private static Label TITLE_ADD_LABEL;
	@PlugKey("databases.dialog.title.edit")
	private static Label TITLE_EDIT_LABEL;
	@PlugKey("databases.dialog.save")
	private static Label LABEL_SAVE;
	@PlugKey("databases.dialog.add")
	private static Label LABEL_ADD;
	@PlugKey("databases.dialog.addonline.confirm")
	private static Label LABEL_ADDONLINE_CONFIRM;

	@PlugKey("databases.dialog.validate.")
	private static String KEYPFX_VALIDATE;

	@Inject
	private MigrationService migrationService;
	@Inject
	private DataSourceService dataSourceService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	@PlugKey("databases.dialog.addonline")
	private Button addOnline;
	@Component
	private TextField description;
	@Component
	private TextField url;
	@Component
	private TextField username;
	@Component
	private TextField password;
	@Component
	private TextField reportingUrl;
	@Component
	private TextField reportingUsername;
	@Component
	private TextField reportingPassword;
	@Component
	private Checkbox useSystem;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);

		addOnline.setClickHandler(
			events.getSubmitValuesHandler("save", true).addValidator(new Confirm(LABEL_ADDONLINE_CONFIRM)));
		addOnline.setComponentAttribute(ButtonType.class, OK_BUTTON_TYPE);

		description.setDefaultRenderer(RendererConstants.TEXTAREA);
		password.setPassword(true);
		reportingPassword.setPassword(true);

		CombinedDisableable disableFields = new CombinedDisableable(useSystem, url, username, password);
		useSystem.setClickHandler(
			new StatementHandler(disableFields.createDisableFunction(), useSystem.createGetExpression()));
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("open");
	}

	@EventHandlerMethod
	public void open(SectionInfo info, long schemaId)
	{
		Model model = getModel(info);
		model.setSchemaId(schemaId);

		if( schemaId != -1 )
		{
			DatabaseSchema ds = migrationService.getSchema(schemaId);
			boolean useSystemFlag = ds.isUseSystem();
			useSystem.setChecked(info, useSystemFlag);
			if( useSystemFlag )
			{
				url.setValue(info, dataSourceService.getSystemUrl());
				username.setValue(info, dataSourceService.getSystemUsername());
			}
			else
			{
				url.setValue(info, ds.getUrl());
				username.setValue(info, ds.getUsername());
			}
			reportingUrl.setValue(info, ds.getReportingUrl());
			reportingUsername.setValue(info, ds.getReportingUsername());
			description.setValue(info, ds.getDescription());
		}
		super.showDialog(info);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		// Call the save event - it will close the dialog with the function that
		// has been set for the Ok callback.
		return events.getSubmitValuesHandler("save", false);
	}

	@EventHandlerMethod
	public void save(SectionInfo info, boolean bringOnline)
	{
		Model model = getModel(info);
		long schemaId = model.getSchemaId();
		boolean create = schemaId <= 0;
		DatabaseSchema ds = new DatabaseSchema(create ? 0 : schemaId);

		String jdbcUrl = url.getValue(info);
		ds.setUrl(Check.isEmpty(jdbcUrl) ? jdbcUrl : jdbcUrl.trim());
		String user = username.getValue(info);
		ds.setUsername(Check.isEmpty(user) ? user : user.trim());
		ds.setPassword(password.getValue(info));
		String rurl = reportingUrl.getValue(info);
		ds.setReportingUrl(Check.isEmpty(rurl) ? rurl : rurl.trim());
		String ruser = reportingUsername.getValue(info);
		ds.setReportingUsername(Check.isEmpty(ruser) ? ruser : ruser.trim());
		ds.setReportingPassword(reportingPassword.getValue(info));
		ds.setDescription(description.getValue(info));
		boolean useSystemFlag = useSystem.isChecked(info);
		ds.setUseSystem(useSystemFlag);

		try
		{
			if( create )
			{
				ds.setOnline(bringOnline);
				schemaId = migrationService.addSchema(ds, bringOnline);
			}
			else
			{
				migrationService.editSchema(ds);
			}
		}
		catch( InvalidDataException ide )
		{
			Map<String, Label> errorMap = Maps.newHashMap();
			List<ValidationError> errors = ide.getErrors();
			for( ValidationError error : errors )
			{
				errorMap.put(error.getField(),
					new KeyLabel(KEYPFX_VALIDATE + error.getField() + '.' + error.getMessage()));
			}
			model.setErrors(errorMap);
			if( useSystemFlag )
			{
				url.setValue(info, dataSourceService.getSystemUrl());
				username.setValue(info, dataSourceService.getSystemUsername());
			}
			return;
		}

		closeDialog(info, new FunctionCallStatement(getOkCallback(), schemaId, create));
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return getModel(context).getSchemaId() > 0 ? TITLE_EDIT_LABEL : TITLE_ADD_LABEL;
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		Model model = getModel(context);

		Class<? extends ExtendedDialect> dialect = dataSourceService.getDialect().getClass();
		if( SQLServerDialect.class.isAssignableFrom(dialect) )
		{
			model.setRequirementsDb("sqlserver");
		}
		else if( ExtendedPostgresDialect.class.isAssignableFrom(dialect) )
		{
			model.setRequirementsDb("postgresql");
		}
		else
		{
			model.setRequirementsDb("oracle");
		}
		if( useSystem.isChecked(context) )
		{
			url.setDisabled(context, true);
			username.setDisabled(context, true);
			password.setDisabled(context, true);
		}

		return viewFactory.createResult("database/databasesdialog.ftl", this);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		List<Button> br = Lists.newArrayListWithExpectedSize(3);

		Label saveLabel = LABEL_SAVE;
		if( getModel(context).getSchemaId() <= 0 )
		{
			saveLabel = LABEL_ADD;
			br.add(addOnline);
		}

		getOk().setLabel(context, saveLabel);
		br.add(getOk());
		return br;
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "databaseeditdialog";
	}

	@Override
	public String getWidth()
	{
		return "700px";
	}

	public TextField getDescription()
	{
		return description;
	}

	public TextField getUrl()
	{
		return url;
	}

	public TextField getUsername()
	{
		return username;
	}

	public TextField getPassword()
	{
		return password;
	}

	public TextField getReportingUrl()
	{
		return reportingUrl;
	}

	public TextField getReportingUsername()
	{
		return reportingUsername;
	}

	public TextField getReportingPassword()
	{
		return reportingPassword;
	}

	@Override
	public Model instantiateDialogModel(SectionInfo info)
	{
		return new Model();
	}

	@Bookmarked(contexts = BookmarkEvent.CONTEXT_SESSION)
	public static class Model extends DialogModel
	{
		@Bookmarked
		private long schemaId;
		private String requirementsDb;
		private Map<String, Label> errors = ImmutableMap.of();

		public long getSchemaId()
		{
			return schemaId;
		}

		public void setSchemaId(long schemaId)
		{
			this.schemaId = schemaId;
		}

		public String getRequirementsDb()
		{
			return requirementsDb;
		}

		public void setRequirementsDb(String requirementsDb)
		{
			this.requirementsDb = requirementsDb;
		}

		public Map<String, Label> getErrors()
		{
			return errors;
		}

		public void setErrors(Map<String, Label> errors)
		{
			this.errors = errors;
		}
	}

	public Checkbox getUseSystem()
	{
		return useSystem;
	}
}
