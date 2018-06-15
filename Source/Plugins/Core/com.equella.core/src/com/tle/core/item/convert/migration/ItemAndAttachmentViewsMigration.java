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

package com.tle.core.item.convert.migration;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import javax.inject.Singleton;
import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Date;
import java.util.List;

@Bind
@Singleton
public class ItemAndAttachmentViewsMigration extends AbstractHibernateSchemaMigration
{
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(ItemAndAttachmentViewsMigration.class) + "."; //$NON-NLS-1$

	//private static final Log LOGGER = LogFactory.getLog(ItemAndAttachmentViewsMigration.class);

	@Override
	@SuppressWarnings("nls")
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return 0;
	}

	@Override
	@SuppressWarnings({"unchecked", "nls"})
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// No-op
	}

	@Override
	@SuppressWarnings("nls")
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> creationSql = helper
			.getCreationSql(new TablesOnlyFilter("item_view", "attachment_view"));
		return creationSql;
	}

	@Override
	@SuppressWarnings("nls")
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return null;
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItemView.class, FakeAttachmentView.class, FakeItem.class, FakeAttachment.class};
	}

	@Override
	@SuppressWarnings("nls")
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "migrate.title", keyPrefix + "migrate.description");
	}

	@AccessType("field")
	@Entity(name = "Item")
	public static class FakeItem
	{
		@Id
		long id;

		@OneToOne(mappedBy = "item", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
		@XStreamOmitField
		private FakeItemView itemView;
	}

	@AccessType("field")
	@Entity(name = "Attachment")
	public static class FakeAttachment
	{
		@Id
		long id;

		@OneToOne(mappedBy = "attachment", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
		@XStreamOmitField
		private FakeAttachmentView attachmentView;
	}

	@Entity(name = "ItemView")
	@AccessType("field")
	@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"item_id"})})
	public static class FakeItemView
	{
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Id
		private long id;

		@OneToOne
		@Index(name = "itemViewItemIndex")
		@JoinColumn(unique = true, nullable = false)
		private FakeItem item;

		@Column(nullable = false)
		@Index(name = "itemViewLastViewedIndex")
		private Date lastViewed;

		@Column(nullable = false)
		@Min(0)
		private int views;
	}

	@Entity(name = "AttachmentView")
	@AccessType("field")
	@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"attachment_id"})})
	public class FakeAttachmentView
	{
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Id
		private long id;

		@OneToOne
		@Index(name = "attachmentViewAttachmentIndex")
		@JoinColumn(unique = true, nullable = false)
		private FakeAttachment attachment;

		@Column(nullable = false)
		@Index(name = "attachmentViewLastViewedIndex")
		private Date lastViewed;

		@Column(nullable = false)
		@Min(0)
		private int views;
	}
}
