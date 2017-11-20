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
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.DatabaseSchema;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationErrorReport;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.MigrationStatusLog;
import com.tle.core.migration.MigrationStatusLog.LogType;
import com.tle.core.migration.SchemaInfo;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.database.ShowErrorDialog.ShowErrorDialogModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.dialog.model.DialogModel;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class ShowErrorDialog extends AbstractOkayableDialog<ShowErrorDialogModel>
{
	@PlugKey("databases.errordialog.title")
	private static Label TITLE;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private MigrationService migrationService;
	@Inject
	private DatabaseTabUtils databaseTabUtils;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
		setOkHandler(new OverrideHandler(getCloseFunction()));
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("open");
	}

	@EventHandlerMethod
	public void open(SectionInfo info, long schemaId)
	{
		ShowErrorDialogModel model = getModel(info);
		model.setSchemaId(schemaId);
		super.showDialog(info);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		final ShowErrorDialogModel model = getModel(context);

		final long schemaId = model.getSchemaId();

		SchemaInfo schemaInfo = migrationService.getMigrationsStatus().getSchemas().get(schemaId);
		DatabaseSchema duplicateWith = schemaInfo.getDuplicateWith();
		if( duplicateWith != null )
		{
			model.setSchemaLabel(databaseTabUtils.getNameRenderer(duplicateWith));
			model.setDupeSchema(duplicateWith);
			return viewFactory.createResult("database/duplicate.ftl", this);
		}

		MigrationErrorReport errorReport = migrationService.getErrorReport(schemaId);
		model.setErrorReport(errorReport);
		StringBuilder sbuf = new StringBuilder();
		List<MigrationStatusLog> log = errorReport.getLog();
		if( !Check.isEmpty(log) )
		{
			for( MigrationStatusLog statLog : log )
			{
				if( statLog.getType() == LogType.SQL )
				{
					sbuf.append(statLog.getValues()[0]);
				}
				else
				{
					sbuf.append(CurrentLocale.get(statLog.getKey(), statLog.getValues()));
				}
				sbuf.append('\n');
			}
			model.setLogAsString(sbuf.toString());
		}

		return viewFactory.createResult("database/errordialog.ftl", this);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return Collections.singleton(getOk());
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE;
	}

	@Override
	public String getWidth()
	{
		return "800px";
	}

	@Override
	public ShowErrorDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new ShowErrorDialogModel();
	}

	public static class ShowErrorDialogModel extends DialogModel
	{
		private long schemaId;
		private MigrationErrorReport errorReport;
		private String logAsString;
		private SectionRenderable schemaLabel;
		private DatabaseSchema dupeSchema;

		public long getSchemaId()
		{
			return schemaId;
		}

		public void setSchemaId(long schemaId)
		{
			this.schemaId = schemaId;
		}

		public MigrationErrorReport getErrorReport()
		{
			return errorReport;
		}

		public void setErrorReport(MigrationErrorReport errorReport)
		{
			this.errorReport = errorReport;
		}

		public SectionRenderable getSchemaLabel()
		{
			return schemaLabel;
		}

		public void setSchemaLabel(SectionRenderable schemaLabel)
		{
			this.schemaLabel = schemaLabel;
		}

		public DatabaseSchema getDupeSchema()
		{
			return dupeSchema;
		}

		public void setDupeSchema(DatabaseSchema dupeSchema)
		{
			this.dupeSchema = dupeSchema;
		}

		public String getLogAsString()
		{
			return logAsString;
		}

		public void setLogAsString(String logAsString)
		{
			this.logAsString = logAsString;
		}

	}
}
