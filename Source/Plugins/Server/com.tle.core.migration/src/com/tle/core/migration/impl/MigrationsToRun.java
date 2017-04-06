package com.tle.core.migration.impl;

import java.io.Serializable;
import java.util.List;

import com.tle.core.migration.impl.MigrationServiceImpl.MigrationState;

public class MigrationsToRun implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final List<MigrationState> migrations;
	private final boolean firstTime;
	private final boolean executions;

	public MigrationsToRun(List<MigrationState> migrations, boolean firstTime, boolean executions)
	{
		this.migrations = migrations;
		this.firstTime = firstTime;
		this.executions = executions;
	}

	public List<MigrationState> getMigrations()
	{
		return migrations;
	}

	public boolean isFirstTime()
	{
		return firstTime;
	}

	public boolean isExecutions()
	{
		return executions;
	}
}