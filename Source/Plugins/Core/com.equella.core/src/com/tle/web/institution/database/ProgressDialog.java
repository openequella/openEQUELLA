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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationStatus;
import com.tle.core.migration.MigrationStatusLog;
import com.tle.core.migration.MigrationSubTaskStatus;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.MigrationJs;
import com.tle.web.institution.database.ProgressDialog.ProgressDialogModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.template.DialogTemplate;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class ProgressDialog extends AbstractOkayableDialog<ProgressDialogModel>
{
	@PlugKey("databases.progress.title")
	private static Label TITLE;
	@PlugKey("databases.progress.finished.general")
	private static String FINISHED_GENERAL_KEY;
	@PlugKey("databases.progress.finished.success")
	private static String FINISHED_SUCCESS_KEY;
	@PlugKey("databases.progress.finished.error")
	private static String FINISHED_ERROR_KEY;
	@PlugKey("databases.progress.runningmigration")
	private static String MIGRATION_KEY;

	@Inject
	private TaskService taskService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	private JSCallable updateResultsCall;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);
		setOkHandler(new OverrideHandler(getCloseFunction()));

		updateResultsCall = ajaxEvents.getAjaxFunction("updateResults");
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("open");
	}

	@EventHandlerMethod
	public void open(SectionInfo info, String taskId)
	{
		ProgressDialogModel model = getModel(info);
		model.setTaskId(taskId);
		super.showDialog(info);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE;
	}

	@Override
	protected TemplateResult getDialogTemplate(RenderContext context)
	{
		getOk().addReadyStatements(
			context,
			new FunctionCallStatement(MigrationJs.SETUP_PROGRESS_DIALOG, updateResultsCall, getModel(context)
				.getTaskId()));

		final SectionRenderable body = viewFactory.createResult("database/progressdialog.ftl", this);
		final SectionRenderable footer = SectionUtils.renderSection(context, getOk());

		return new GenericTemplateResult().addNamedResult(DialogTemplate.BODY, body).addNamedResult(
			DialogTemplate.FOOTER, footer);
	}

	@AjaxMethod
	public ProgressUpdate updateResults(SectionInfo info, String taskId, int logOffset)
	{
		TaskStatus taskStatus = taskService.waitForTaskStatus(taskId, TimeUnit.SECONDS.toMillis(20));
		taskService.askTaskChanges(Collections.singleton(taskId));

		Collection<String> warnings = Lists.newArrayList();
		Collection<String> messages = Lists.newArrayList();

		Pair<Integer, List<MigrationStatusLog>> taskLogs = taskStatus.getTaskLog(logOffset, Integer.MAX_VALUE);
		logOffset = taskLogs.getFirst();
		List<MigrationStatusLog> logs = taskLogs.getSecond();
		for( MigrationStatusLog log : logs )
		{
			switch( log.getType() )
			{
				case WARNING:
					warnings.add(CurrentLocale.get(log.getKey(), log.getValues()));
					break;
				case MESSAGE:
					messages.add(CurrentLocale.get(log.getKey(), log.getValues()));
					break;
				case SQL:
					messages.add((String) log.getValues()[0]);
					break;
			}
		}

		final String message;
		final String submessage;
		if( taskStatus.isFinished() )
		{
			message = CurrentLocale.get(FINISHED_GENERAL_KEY);
			submessage = CurrentLocale.get(Check.isEmpty(taskStatus.getErrorMessage()) ? FINISHED_SUCCESS_KEY
				: FINISHED_ERROR_KEY);
		}
		else
		{
			MigrationInfo currentMigration = getCurrentMigration(taskStatus);
			String migrationName = "";
			if( currentMigration != null )
			{
				migrationName = currentMigration.getName().toString();
			}
			message = CurrentLocale
				.get(MIGRATION_KEY, taskStatus.getDoneWork(), taskStatus.getMaxWork(), migrationName);

			final MigrationSubTaskStatus msts = taskStatus.getTaskSubStatus(MigrationStatus.KEY_EXECUTION_STATUS);
			if( msts != null )
			{
				submessage = CurrentLocale.get(msts.getStatusKey(), msts.getCurrentDone(), msts.getCurrentMax());
			}
			else
			{
				submessage = "";
			}
		}
		return new ProgressUpdate(logOffset, message, submessage, warnings, messages, taskStatus.isFinished());
	}

	@Nullable
	private MigrationInfo getCurrentMigration(TaskStatus ts)
	{
		List<MigrationInfo> migInfos = ts.getTaskSubStatus(MigrationStatus.KEY_MIGRATION_INFOS);
		for( MigrationInfo migrationInfo : migInfos )
		{
			if( migrationInfo.isExecuting() )
			{
				return migrationInfo;
			}
		}
		return null;
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "databaseprogressdialog";
	}

	@Override
	public String getWidth()
	{
		return "600px";
	}

	@Override
	public ProgressDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new ProgressDialogModel();
	}

	public static class ProgressDialogModel extends DialogModel
	{
		private String taskId;
		private int runningMigration;
		private int totalMigrations;
		private String runningMigrationName;
		private int runningStep;
		private int totalSteps;

		public String getRunningMigrationName()
		{
			return runningMigrationName;
		}

		public void setRunningMigrationName(String runningMigrationName)
		{
			this.runningMigrationName = runningMigrationName;
		}

		public int getRunningStep()
		{
			return runningStep;
		}

		public void setRunningStep(int runningStep)
		{
			this.runningStep = runningStep;
		}

		public int getTotalSteps()
		{
			return totalSteps;
		}

		public void setTotalSteps(int totalSteps)
		{
			this.totalSteps = totalSteps;
		}

		public String getTaskId()
		{
			return taskId;
		}

		public void setTaskId(String taskId)
		{
			this.taskId = taskId;
		}

		public int getRunningMigration()
		{
			return runningMigration;
		}

		public void setRunningMigration(int runningMigration)
		{
			this.runningMigration = runningMigration;
		}

		public int getTotalMigrations()
		{
			return totalMigrations;
		}

		public void setTotalMigrations(int totalMigrations)
		{
			this.totalMigrations = totalMigrations;
		}
	}

	public static class ProgressUpdate
	{
		private final String migration;
		private final String step;
		private final Collection<String> warnings;
		private final Collection<String> messages;
		private final boolean finished;
		private final int offset;

		public ProgressUpdate(int offset, String migration, String step, Collection<String> warnings,
			Collection<String> messages,
			boolean finished)
		{
			this.offset = offset;
			this.migration = migration;
			this.step = step;
			this.warnings = warnings;
			this.messages = messages;
			this.finished = finished;
		}

		public int getOffset()
		{
			return offset;
		}

		public String getMigration()
		{
			return migration;
		}

		public String getStep()
		{
			return step;
		}

		public Collection<String> getWarnings()
		{
			return warnings;
		}

		public Collection<String> getMessages()
		{
			return messages;
		}

		public boolean isFinished()
		{
			return finished;
		}
	}
}
