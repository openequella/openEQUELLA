package com.tle.core.workflow.migrate;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.tle.core.guice.Bind;
import com.tle.core.migration.AbstractCombinedSchemaMigration;
import com.tle.core.migration.AbstractHibernateSchemaMigration;

@Bind
@Singleton
public class AddNotificationSchema extends AbstractCombinedSchemaMigration
{
	private final ImmutableList<AbstractHibernateSchemaMigration> migrations;

	@Inject
	public AddNotificationSchema(AddNotificationSchemaOrig original, AddTaskStartDate addLast,
		CreateTaskHistoryTable addTaskHistory)
	{
		migrations = ImmutableList.of(original, addLast, addTaskHistory);
	}

	@Override
	protected List<AbstractHibernateSchemaMigration> getMigrations()
	{
		return migrations;
	}

}
