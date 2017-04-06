package com.tle.core.migration;

public interface Migration
{
	boolean isBackwardsCompatible();

	void migrate(MigrationResult status) throws Exception;

	MigrationInfo createMigrationInfo();
}
