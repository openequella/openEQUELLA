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

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.DatabaseSchema;
import com.tle.core.guice.Bind;
import com.tle.core.migration.MigrationService;
import com.tle.core.migration.SchemaInfo;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class SelectDatabaseDialog extends AbstractOkayableDialog<DialogModel>
{
	@PlugKey("databases.select.title")
	private static Label TITLE;

	@Inject
	private MigrationService migrationService;
	@Inject
	private DatabaseTabUtils databaseTabUtils;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	private SingleSelectionList<Void> list;

	public Iterable<DatabaseSchema> getOnlineSchemas()
	{
		return Iterables.transform(
			Iterables.filter(migrationService.getMigrationsStatus().getSchemas().values(), new Predicate<SchemaInfo>()
			{
				@Override
				public boolean apply(SchemaInfo input)
				{
					return input.isUp() && !input.isSystem();
				}
			}), new Function<SchemaInfo, DatabaseSchema>()
			{
				@Override
				public DatabaseSchema apply(SchemaInfo input)
				{
					return input.getDatabaseSchema();
				}
			});
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		setAjax(true);

		list.setDefaultRenderer(RendererConstants.CHECKLIST);
		list.setAlwaysSelect(true);
		list.setListModel(new DynamicHtmlListModel<Void>()
		{
			@Override
			protected Iterable<Option<Void>> populateOptions(SectionInfo info)
			{
				return Iterables.transform(getOnlineSchemas(), new Function<DatabaseSchema, Option<Void>>()
				{
					@Override
					public Option<Void> apply(DatabaseSchema ds)
					{
						return new LabelOption<Void>(databaseTabUtils.getNameLabel(ds), Long.toString(ds.getId()), null);
					}
				});
			}

			@Override
			protected Iterable<Void> populateModel(SectionInfo info)
			{
				return null;
			}
		});
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE;
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("database/selectdatabasedialog.ftl", this);
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		return new OverrideHandler(jscall(getOkCallback(), list.createGetExpression()), jscall(getCloseFunction()));
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "databaseselectdialog";
	}

	@Override
	public String getWidth()
	{
		return "400px";
	}

	public SingleSelectionList<Void> getList()
	{
		return list;
	}

	@Override
	public DialogModel instantiateDialogModel(SectionInfo info)
	{
		return new DialogModel();
	}
}
