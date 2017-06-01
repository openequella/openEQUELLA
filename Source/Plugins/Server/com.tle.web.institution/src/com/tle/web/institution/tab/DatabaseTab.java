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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tle.beans.DatabaseSchema;
import com.tle.common.Check;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.MigrationStatus;
import com.tle.core.migration.SchemaInfo;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.institution.MigrationJs;
import com.tle.web.institution.database.DatabaseEditDialog;
import com.tle.web.institution.database.DatabaseTabUtils;
import com.tle.web.institution.database.ProgressDialog;
import com.tle.web.institution.database.ShowErrorDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AbstractDOMResult;
import com.tle.web.sections.ajax.AjaxCaptureOptions;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.FullAjaxCaptureResult;
import com.tle.web.sections.ajax.FullDOMResult;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.BootstrapSplitDropDownRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.PageUniqueId;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.NotExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.ReturnStatement;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.js.validators.FunctionCallValidator;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.FormValuesLibrary;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.template.Decorations;

@SuppressWarnings("nls")
public class DatabaseTab extends AbstractInstitutionTab<DatabaseTab.Model>
{
	@PlugKey("databases.title")
	private static Label TITLE_LABEL;
	@PlugKey("databases.link.name")
	private static Label LINK_LABEL;
	@PlugKey("databases.migrate")
	private static Label LABEL_MIGRATE;
	@PlugKey("databases.migrate.confirm")
	private static Label LABEL_MIGRATE_CONFIRM;
	@PlugKey("databases.progress")
	private static Label LABEL_PROGRESS;
	@PlugKey("databases.review")
	private static Label LABEL_REVIEW;

	@PlugKey("databases.add.receipt")
	private static Label ADD_RECEIPT;
	@PlugKey("databases.bringonline")
	private static Label BRING_ONLINE_LABEL;
	@PlugKey("databases.bringonline.receipt")
	private static Label BRING_ONLINE_RECEIPT;
	@PlugKey("databases.takeoffline")
	private static Label TAKE_OFFLINE_LABEL;
	@PlugKey("databases.takeoffline.confirm")
	private static Confirm TAKE_OFFLINE_CONFIRM;
	@PlugKey("databases.takeoffline.receipt")
	private static Label TAKE_OFFLINE_RECEIPT;
	@PlugKey("databases.edit")
	private static Label EDIT_LABEL;
	@PlugKey("databases.edit.receipt")
	private static Label EDIT_RECEIPT;
	@PlugKey("databases.delete")
	private static Label DELETE_LABEL;
	@PlugKey("databases.delete.confirm")
	private static Confirm DELETE_CONFIRM;
	@PlugKey("databases.delete.receipt")
	private static Label DELETE_RECEIPT;

	@PlugKey("databases.refresh")
	private static Label LABEL_REFRESH;
	@PlugKey("databases.initialise")
	private static Label LABEL_INITIALISE;

	@PlugKey("databases.status.online")
	private static Label LABEL_ONLINE;
	@PlugKey("databases.status.offline")
	private static Label LABEL_OFFLINE;
	@PlugKey("databases.status.migrating")
	private static Label LABEL_MIGRATING;
	@PlugKey("databases.status.reqmigrating")
	private static Label LABEL_REQUIRES_MIGRATING;
	@PlugKey("databases.status.unavailable")
	private static Label LABEL_UNAVAILABLE;
	@PlugKey("databases.status.needsinit")
	private static Label LABEL_NEEDSINIT;
	@PlugKey("databases.status.dberror")
	private static Label LABEL_DB_ERROR;
	@PlugKey("databases.status.retry")
	private static Label LABEL_RETRY;
	@PlugKey("databases.status.failed")
	private static Label LABEL_FAILED;
	@PlugKey("databases.status.duplicate")
	private static Label LABEL_DUPLICATE;
	@PlugKey("databases.status.checking")
	private static Label LABEL_CHECKING;

	@PlugKey("databases.showduplicate")
	private static Label LABEL_DUPLICATE_ERROR;

	@PlugKey("databases.migerror")
	private static Label LABEL_MIG_ERROR;

	@PlugKey("databases.cell.name")
	private static Label LABEL_COL_NAME;
	@PlugKey("databases.cell.status")
	private static Label LABEL_COL_STATUS;
	@PlugKey("databases.cell.actions")
	private static Label LABEL_COL_ACTIONS;

	private static enum Status
	{
		ONLINE("online"), OFFLINE("offline"), NEEDS_ATTENTION("needsattention"), BEING_PROCESSED("beingprocessed");

		private final String cssClass;

		private Status(String cssClass)
		{
			this.cssClass = cssClass;
		}

		public String getCssClass()
		{
			return cssClass;
		}
	}

	@Inject
	private ReceiptService receiptService;
	@Inject
	private MigrationService migrationService;
	@Inject
	private TaskService taskService;
	@Inject
	private DatabaseTabUtils databaseTabUtils;

	@Inject
	@Component(name = "ded")
	private DatabaseEditDialog editDialog;
	@Inject
	@Component
	private ShowErrorDialog errorsDialog;
	@Inject
	@Component
	private ProgressDialog progressDialog;
	@Component(stateful = false)
	private MappedBooleans bulkBoxes;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	private Table table;

	@Component
	@PlugKey("databases.add")
	private Link add;
	@Component
	@PlugKey("databases.migrateselected")
	private Link migrateSelectedButton;

	private JSCallable setOnlineCallable;
	private JSCallable deleteCallable;
	private JSCallable migrateCallable;
	private JSCallable ajaxUpdate;

	private Gson gson = new GsonBuilder().serializeNulls().create();
	private FunctionCallValidator migrateConfirm;
	private StatementHandler disableHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		add.setClickHandler(new OverrideHandler(editDialog.getOpenFunction(), -1));
		editDialog.setOkCallback(events.getSubmitValuesFunction("schemaChanged"));

		setOnlineCallable = events.getSubmitValuesFunction("setSchemaOnline");
		deleteCallable = events.getSubmitValuesFunction("deleteSchema");
		migrateCallable = events.getSubmitValuesFunction("executeMigrations");
		migrateConfirm = new FunctionCallValidator(new SimpleFunction("confirmMigrate", new ReturnStatement(
			new FunctionCallExpression(Confirm.CONFIRM, LABEL_MIGRATE_CONFIRM))));
		migrateSelectedButton.setDisablable(true);
		migrateSelectedButton.setClickHandler(events.getNamedHandler("migrateSelected").addValidator(migrateConfirm));
		migrateSelectedButton.setDefaultRenderer(RendererConstants.LINK);
		ajaxUpdate = ajax.getAjaxFunction("updatePage");

		// Column[2] is the checkbox
		table.setColumnHeadings(LABEL_COL_NAME, LABEL_COL_STATUS, null, LABEL_COL_ACTIONS).getCells().get(3)
			.addClass("actionsheader");

		disableHandler = new StatementHandler(Js.call_s(migrateSelectedButton.createDisableFunction(),
			new NotExpression(Js.call(FormValuesLibrary.IS_SOME_CHECKED, bulkBoxes.getParameterId()))));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final Model model = getModel(context);
		MigrationStatus migStatus = migrationService.getMigrationsStatus();
		List<SchemaInfo> schemas = Lists.newArrayList(migStatus.getSchemas().values());
		Map<Long, Long> updateTimes = model.getUpdateTimes();
		if( updateTimes == null )
		{
			updateTimes = Maps.newHashMap();
		}
		Collections.sort(schemas, new DatabaseComparator());
		TableState tableState = table.getState(context);
		TableUpdates updates = new TableUpdates();

		boolean hasMigrations = false;

		for( SchemaInfo schemaInfo : schemas )
		{
			DatabaseSchema ds = schemaInfo.getDatabaseSchema();

			final ObjectExpression handlers = new ObjectExpression();
			final HtmlListState actions = new HtmlListState();
			actions.setElementId(new SimpleElementId("schema" + ds.getId()));
			actions.setEventHandler(JSHandler.EVENT_CHANGE,
				Js.handler(MigrationJs.HANDLER_DISPATCHER, Jq.$(actions), handlers));
			actions.setDisallowMultiple(true);
			actions.setOptions(Lists.<Option<?>> newArrayList());

			long schemaId = ds.getId();
			if( schemaInfo.isSystem() )
			{
				continue;
			}
			if( model.isPartial() )
			{
				Long updateTime = updateTimes.get(schemaId);
				if( updateTime != null && schemaInfo.getUpdateTime() == updateTime )
				{
					if( schemaInfo.isChecking() )
					{
						updates.someUpdating = true;
					}
					continue;
				}
			}
			final TableRow row = tableState.addRow();
			if( model.isPartial() )
			{
				row.setAjaxCaptureOptions(new AjaxCaptureOptions("schemaRow", true, true, ImmutableMap.of("id",
					(Object) schemaId)));
			}
			SimpleElementId rowId = new SimpleElementId("schemaRow_" + schemaId);
			row.setElementId(rowId);
			row.addCell(new TableCell(databaseTabUtils.getNameRenderer(ds)));

			DatabaseRow drow = new DatabaseRow();
			drow.updateTime = schemaInfo.getUpdateTime();

			if( schemaInfo.isChecking() )
			{
				drow.status = Status.BEING_PROCESSED;
				drow.statusLabel = LABEL_CHECKING;
				drow.editDelete = true;
				updates.someUpdating = true;
			}
			else if( schemaInfo.isUp() )
			{
				createOption(TAKE_OFFLINE_LABEL, actions, handlers, "action_takedown", new OverrideHandler(
					setOnlineCallable, schemaId, false).addValidator(TAKE_OFFLINE_CONFIRM), true);

				String finishedTaskId = schemaInfo.getFinishedTaskId();
				if( !Check.isEmpty(finishedTaskId) )
				{
					TaskStatus taskStatus = taskService.getTaskStatus(finishedTaskId);
					if( taskStatus != null )
					{
						createOption(LABEL_REVIEW, actions, handlers, "action_review", new OverrideHandler(
							progressDialog.getOpenFunction(), finishedTaskId), false);
					}
				}
			}
			else
			{
				drow.editDelete = true;
				if( schemaInfo.getErrorMessage() != null )
				{
					drow.status = Status.NEEDS_ATTENTION;
					drow.statusLabel = LABEL_UNAVAILABLE;
					drow.errorLink = createErrorLink(schemaInfo, LABEL_DB_ERROR);
				}
				else if( schemaInfo.isMigrationRequired() )
				{
					hasMigrations = true;
					processMigrationRequired(context, schemaInfo, drow, actions, handlers, updates, schemas.size() > 2);
				}
				else
				{
					if( schemaInfo.getDuplicateWith() != null )
					{
						drow.status = Status.NEEDS_ATTENTION;
						drow.statusLabel = LABEL_DUPLICATE;
						drow.errorLink = createErrorLink(schemaInfo, LABEL_DUPLICATE_ERROR);
					}
					else
					{
						drow.status = Status.OFFLINE;
						drow.statusLabel = LABEL_OFFLINE;
						if( !schemaInfo.getDatabaseSchema().isOnline() )
						{
							createOption(BRING_ONLINE_LABEL, actions, handlers, "action_online", new OverrideHandler(
								setOnlineCallable, schemaId, true), true);
						}
					}
				}
			}

			addCells(row, drow, actions, handlers, schemaId);

			updateTimes.put(schemaId, drow.updateTime);
		}
		model.setNoSchemas(tableState.getRows().isEmpty());
		model.setSomeUpdating(updates.someUpdating);
		migrateSelectedButton.setDisplayed(context, schemas.size() > 2);

		boolean showLink = hasMigrations && schemas.size() > 2;
		migrateSelectedButton.setDisplayed(context, showLink);
		if( showLink )
		{
			migrateSelectedButton.addReadyStatements(context, disableHandler);
		}

		Decorations d = Decorations.getDecorations(context);
		d.setTitle(TITLE_LABEL);
		taskService.askTaskChanges(updates.askChanges);

		if( !model.isPartial() && updates.someUpdating )
		{
			context.getBody().addReadyStatements(MigrationJs.SETUP_PROGRESS, ajaxUpdate, updateTimes);
		}
		return viewFactory.createResult("database/databases.ftl", context);
	}

	private void addCells(TableRow row, DatabaseRow drow, HtmlListState actions, ObjectExpression handlers,
		long schemaId)
	{
		if( drow.editDelete )
		{
			createOption(EDIT_LABEL, actions, handlers, "action_edit", new OverrideHandler(
				editDialog.getOpenFunction(), schemaId), false);
			createOption(DELETE_LABEL, actions, handlers, "action_delete",
				new OverrideHandler(deleteCallable, schemaId).addValidator(DELETE_CONFIRM), false);
		}

		createOption(LABEL_REFRESH, actions, handlers, "action_refresh",
			events.getNamedHandler("refreshSchema", schemaId), Check.isEmpty(actions.getSelectedValues()));

		TableCell statusCell = new TableCell(drow.statusLabel).addClass("status statuscol");
		if( drow.errorLink != null )
		{
			statusCell.addContent(new DivRenderer("errorlink", drow.errorLink));
		}
		if( drow.progress != null )
		{
			statusCell.addContent(new DivRenderer("progress", drow.progress));
		}
		row.addCell(statusCell);

		row.addCell(drow.checkbox != null ? drow.checkbox : null).addClass("statuscol");

		TableCell actionCell = new TableCell().addClass("actions statuscol");
		final BootstrapSplitDropDownRenderer splitButton = new BootstrapSplitDropDownRenderer(actions);
		splitButton.addDropdownClass("pull-right");
		actionCell.addContent(splitButton);
		row.addCell(actionCell);

		row.addClass(drow.status.getCssClass());
	}

	private Option<String> createOption(Label label, HtmlListState actions, ObjectExpression handlers, String key,
		JSHandler handler, boolean isDefault)
	{
		handlers.put(key, CallAndReferenceFunction.get(Js.function(handler),
			new AppendedElementId(actions.getWrappedElementId(), new PageUniqueId())));
		if( isDefault )
		{
			actions.setSelectedValues(key);
		}
		Option<String> option = new LabelOption<String>(label, key, key);
		actions.getOptions().add(option);
		return option;
	}

	private LinkRenderer createErrorLink(SchemaInfo schemaInfo, Label label)
	{
		HtmlLinkState state = new HtmlLinkState();
		state.setLabel(label);
		state.addClass("error");
		state.setClickHandler(new OverrideHandler(errorsDialog.getOpenFunction(), schemaInfo.getDatabaseSchema()
			.getId()));
		return new LinkRenderer(state);
	}

	private void processMigrationRequired(RenderContext context, SchemaInfo schemaInfo, DatabaseRow drow,
		HtmlListState actions, ObjectExpression handlers, TableUpdates updates, boolean multipleSchemas)
	{
		drow.status = Status.NEEDS_ATTENTION;
		drow.statusLabel = LABEL_REQUIRES_MIGRATING;
		String taskId = schemaInfo.getTaskId();
		if( taskId != null )
		{
			drow.updateTime = -1L;
			updates.someUpdating = true;
			drow.status = Status.BEING_PROCESSED;
			drow.statusLabel = LABEL_MIGRATING;

			createOption(LABEL_PROGRESS, actions, handlers, "action_progress",
				new OverrideHandler(progressDialog.getOpenFunction(), taskId), true);

			TaskStatus taskStatus = taskService.waitForTaskStatus(taskId, 5000);
			String statusKey = taskStatus.getStatusKey();
			if( statusKey != null )
			{
				drow.progress = new KeyLabel(taskStatus.getStatusKey(), taskStatus.getDoneWork(),
					taskStatus.getMaxWork(), taskStatus.getPercentage());
			}

			updates.askChanges.add(taskId);
			drow.editDelete = false;
		}
		else
		{
			Label migrateLabel = LABEL_MIGRATE;
			boolean canMigrate = true;
			if( schemaInfo.isHasErrors() )
			{
				drow.errorLink = createErrorLink(schemaInfo, LABEL_MIG_ERROR);
				if( schemaInfo.isCanRetry() )
				{
					drow.statusLabel = LABEL_RETRY;
				}
				else
				{
					drow.statusLabel = LABEL_FAILED;
					canMigrate = false;
				}
			}
			else if( schemaInfo.isInitial() )
			{
				drow.statusLabel = LABEL_NEEDSINIT;
				migrateLabel = LABEL_INITIALISE;
			}
			if( canMigrate )
			{
				DatabaseSchema ds = schemaInfo.getDatabaseSchema();
				long schemaId = ds.getId();
				if( multipleSchemas )
				{
					drow.checkbox = bulkBoxes.getBooleanState(context, Long.toString(schemaId));
					drow.checkbox.setClickHandler(disableHandler);
				}
				OverrideHandler migrateHandler = new OverrideHandler(migrateCallable, schemaId);
				if( !schemaInfo.isInitial() )
				{
					migrateHandler.addValidator(migrateConfirm);
				}
				createOption(migrateLabel, actions, handlers, "action_migrate", migrateHandler, true);
			}
		}
	}

	private static final class DatabaseRow
	{
		long updateTime;
		Status status = Status.ONLINE;
		Label statusLabel = LABEL_ONLINE;

		LinkRenderer errorLink;
		Label progress;
		HtmlBooleanState checkbox;

		boolean editDelete;
	}

	private static final class TableUpdates
	{
		boolean someUpdating;
		Set<String> askChanges = Sets.newHashSet();

	}

	@EventHandlerMethod
	public void refreshSchema(SectionInfo info, long schemaId)
	{
		migrationService.refreshSchema(schemaId);
	}

	@EventHandlerMethod
	public void executeMigrations(SectionInfo info, long schemaId)
	{
		migrationService.executeMigrationsForSchemas(Collections.singleton(schemaId));
	}

	@EventHandlerMethod
	public void migrateSelected(SectionInfo info)
	{
		Set<String> schemaStrs = bulkBoxes.getCheckedSet(info);
		Collection<Long> schemaIds = Collections2.transform(schemaStrs, new Function<String, Long>()
		{
			@Override
			public Long apply(String input)
			{
				return Long.parseLong(input);
			}
		});
		migrationService.executeMigrationsForSchemas(schemaIds);
	}

	@EventHandlerMethod
	public void schemaChanged(SectionInfo info, long schemaId, boolean created)
	{
		receiptService.setReceipt(created ? ADD_RECEIPT : EDIT_RECEIPT);
	}

	@EventHandlerMethod
	public void setSchemaOnline(SectionInfo info, long schemaId, boolean online)
	{
		migrationService.setSchemasOnline(Collections.singleton(schemaId), online);
		receiptService.setReceipt(online ? BRING_ONLINE_RECEIPT : TAKE_OFFLINE_RECEIPT);
	}

	@EventHandlerMethod
	public void deleteSchema(SectionInfo info, long schemaId)
	{
		migrationService.deleteSchema(schemaId);
		receiptService.setReceipt(DELETE_RECEIPT);
	}

	public class DatabaseComparator extends NumberStringComparator<SchemaInfo>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public String convertToString(SchemaInfo s)
		{
			if( s.isSystem() )
			{
				return "";
			}
			return databaseTabUtils.getNameLabel(s.getDatabaseSchema()).getText();
		}
	}

	@AjaxMethod
	public JSONResponseCallback updatePage(final AjaxRenderContext info, Map<Long, Long> updateTimes)
	{
		info.addAjaxDivs("schemaRow", "migratecontainer");
		final Model model = getModel(info);
		model.setPartial(true);
		model.setUpdateTimes(updateTimes);
		return new JSONResponseCallback()
		{
			@Override
			public Object getResponseObject(AjaxRenderContext context)
			{
				FullDOMResult ajaxResult = context.getFullDOMResult();
				DatabaseStatusUpdate update = new DatabaseStatusUpdate(ajaxResult);
				update.setUpdateTimes(model.getUpdateTimes());
				update.setRows(ajaxResult.getLists().get("schemaRow"));
				update.setUpdates(ajaxResult.getHtml());
				update.setSomeUpdating(model.isSomeUpdating());
				return new SimpleSectionResult(gson.toJson(update));
			}
		};
	}

	public static class DatabaseStatusUpdate extends AbstractDOMResult
	{
		private Map<Long, Long> updateTimes;
		private Map<String, FullAjaxCaptureResult> updates;
		private List<FullAjaxCaptureResult> rows;
		private boolean someUpdating;

		public DatabaseStatusUpdate(FullDOMResult ajaxResult)
		{
			super(ajaxResult);
		}

		public Map<String, FullAjaxCaptureResult> getUpdates()
		{
			return updates;
		}

		public void setUpdates(Map<String, FullAjaxCaptureResult> updates)
		{
			this.updates = updates;
		}

		public List<FullAjaxCaptureResult> getRows()
		{
			return rows;
		}

		public void setRows(List<FullAjaxCaptureResult> rows)
		{
			this.rows = rows;
		}

		public Map<Long, Long> getUpdateTimes()
		{
			return updateTimes;
		}

		public void setUpdateTimes(Map<Long, Long> updateTimes)
		{
			this.updateTimes = updateTimes;
		}

		public boolean isSomeUpdating()
		{
			return someUpdating;
		}

		public void setSomeUpdating(boolean someUpdating)
		{
			this.someUpdating = someUpdating;
		}
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public Label getName()
	{
		return LINK_LABEL;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		private boolean noSchemas;
		private boolean someUpdating;
		private Map<Long, Long> updateTimes;
		private boolean partial;

		public boolean isPartial()
		{
			return partial;
		}

		public void setPartial(boolean partial)
		{
			this.partial = partial;
		}

		public boolean isNoSchemas()
		{
			return noSchemas;
		}

		public void setNoSchemas(boolean noSchemas)
		{
			this.noSchemas = noSchemas;
		}

		public Map<Long, Long> getUpdateTimes()
		{
			return updateTimes;
		}

		public void setUpdateTimes(Map<Long, Long> updateTimes)
		{
			this.updateTimes = updateTimes;
		}

		public boolean isSomeUpdating()
		{
			return someUpdating;
		}

		public void setSomeUpdating(boolean someUpdating)
		{
			this.someUpdating = someUpdating;
		}
	}

	public Link getAdd()
	{
		return add;
	}

	public Link getMigrateSelectedButton()
	{
		return migrateSelectedButton;
	}

	public Table getTable()
	{
		return table;
	}
}