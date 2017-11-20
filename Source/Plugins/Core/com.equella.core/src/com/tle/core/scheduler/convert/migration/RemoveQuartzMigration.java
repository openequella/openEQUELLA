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

package com.tle.core.scheduler.convert.migration;

import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveQuartzMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(RemoveQuartzMigration.class)
		+ ".removequartz.";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "title", keyPrefix + "description");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeQrtzBlobTriggers.class, FakeQrtzCalendars.class, FakeQrtzCronTriggers.class,
				FakeQrtzFiredTriggers.class, FakeQrtzJobDetails.class, FakeQrtzJobListeners.class, FakeQrtzLocks.class,
				FakeQrtzPausedTriggerGrps.class, FakeQrtzSchedulerState.class, FakeQrtzSimpleTriggers.class,
				FakeQrtzTriggerListeners.class, FakeQrtzTriggers.class, FakeConfigurationProperty.class};
	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return false;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		return Collections.emptyList();
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropTableSql("qrtz_blob_triggers", "qrtz_calendars", "qrtz_cron_triggers",
			"qrtz_fired_triggers", "qrtz_job_listeners", "qrtz_locks", "qrtz_paused_trigger_grps",
			"qrtz_scheduler_state", "qrtz_simple_triggers", "qrtz_trigger_listeners", "qrtz_triggers",
			"qrtz_job_details");
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Remove old scheduler properties
		session.createSQLQuery("DELETE FROM configuration_property WHERE property LIKE 'scheduler.jobs.%'")
			.executeUpdate();
		result.incrementStatus();
	}

	@Entity(name = "ConfigurationProperty")
	@AccessType("field")
	public class FakeConfigurationProperty
	{
		@Id
		String property;
	}

	@Entity(name = "QrtzBlobTriggers")
	@AccessType("field")
	public class FakeQrtzBlobTriggers
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzCalendars")
	@AccessType("field")
	public class FakeQrtzCalendars
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzCronTriggers")
	@AccessType("field")
	public class FakeQrtzCronTriggers
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzFiredTriggers")
	@AccessType("field")
	public class FakeQrtzFiredTriggers
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzJobDetails")
	@AccessType("field")
	public class FakeQrtzJobDetails
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzJobListeners")
	@AccessType("field")
	public class FakeQrtzJobListeners
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzLocks")
	@AccessType("field")
	public class FakeQrtzLocks
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzPausedTriggerGrps")
	@AccessType("field")
	public class FakeQrtzPausedTriggerGrps
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzSchedulerState")
	@AccessType("field")
	public class FakeQrtzSchedulerState
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzSimpleTriggers")
	@AccessType("field")
	public class FakeQrtzSimpleTriggers
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzTriggerListeners")
	@AccessType("field")
	public class FakeQrtzTriggerListeners
	{
		@Id
		long fakeId;
	}

	@Entity(name = "QrtzTriggers")
	@AccessType("field")
	public class FakeQrtzTriggers
	{
		@Id
		long fakeId;
	}
}
