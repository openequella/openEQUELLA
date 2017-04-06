package com.tle.core.migration;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.tle.beans.DatabaseSchema;
import com.tle.core.migration.impl.MigrationServiceImpl.MigrationExt;
import com.tle.core.migration.impl.MigrationServiceImpl.MigrationState;

public interface MigrationService
{
	MigrationStatus getMigrationsStatus();

	void executeMigrationsForSchemas(Collection<Long> schemaIds);

	List<MigrationStatusLog> getWarnings(String taskId);

	Migration getMigration(MigrationState ext);

	void refreshSchema(long schemaId);

	void setSchemasOnline(Collection<Long> schemaIds, boolean online);

	boolean isSomeSchemasUp();

	DatabaseSchema getSchema(long schemaId);

	SchemaInfo getSystemSchemaInfo();

	long addSchema(DatabaseSchema ds, boolean initialise);

	void editSchema(DatabaseSchema ds);

	void deleteSchema(long schemaId);

	Collection<Long> getAvailableSchemaIds();

	boolean isSystemSchemaUp();

	void setInstallSettings(InstallSettings installSettings);

	InstallSettings getInstallSettings();

	Set<MigrationExt> getOrderedMigrations();

	MigrationErrorReport getErrorReport(long schemaId);

	void refreshSystemSchema();

	void migrateSystemSchema();
}
