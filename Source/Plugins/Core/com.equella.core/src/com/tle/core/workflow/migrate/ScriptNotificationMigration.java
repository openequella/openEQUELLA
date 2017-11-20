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

package com.tle.core.workflow.migrate;

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
public class ScriptNotificationMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(ScriptNotificationMigration.class) + ".";

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "script.notification.migration.title");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		result.incrementStatus();
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 1;
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> creationSql = helper.getCreationSql(new TablesOnlyFilter("wf_script_notify_completion_u",
			"wf_script_notify_completion_g", "wf_script_notify_error_u", "wf_script_notify_error_g"));
		creationSql.addAll(helper.getAddColumnsSQL("workflow_node", "notify_on_completion", "notify_on_error"));
		return creationSql;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeWorkflowNode.class};
	}

	@Entity(name = "WorkflowNode")
	@AccessType("field")
	public static class FakeWorkflowNode
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		boolean notifyOnCompletion;
		boolean notifyOnError;

		@ElementCollection
		@Column(name = "`user`", length = 255)
		@CollectionTable(name = "wf_script_notify_completion_u", joinColumns = @JoinColumn(name = "workflow_node_id"))
		private Set<String> usersNotifyOnCompletion;
		@ElementCollection
		@Column(name = "`group`", length = 255)
		@CollectionTable(name = "wf_script_notify_completion_g", joinColumns = @JoinColumn(name = "workflow_node_id"))
		private Set<String> groupsNotifyOnCompletion;

		@ElementCollection
		@Column(name = "`user`", length = 255)
		@CollectionTable(name = "wf_script_notify_error_u", joinColumns = @JoinColumn(name = "workflow_node_id"))
		private Set<String> usersNotifyOnError;
		@ElementCollection
		@Column(name = "`group`", length = 255)
		@CollectionTable(name = "wf_script_notify_error_g", joinColumns = @JoinColumn(name = "workflow_node_id"))
		private Set<String> groupsNotifyOnError;
	}

}
