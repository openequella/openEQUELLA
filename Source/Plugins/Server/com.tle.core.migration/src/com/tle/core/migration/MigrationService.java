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
