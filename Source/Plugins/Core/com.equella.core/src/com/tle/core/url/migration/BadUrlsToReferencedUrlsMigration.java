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

package com.tle.core.url.migration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.classic.Session;

import com.google.common.collect.Sets;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractHibernateSchemaMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;

@Bind
@Singleton
@SuppressWarnings("nls")
public class BadUrlsToReferencedUrlsMigration extends AbstractHibernateSchemaMigration
{
	private static final String migInfo = PluginServiceImpl.getMyPluginId(BadUrlsToReferencedUrlsMigration.class) + ".migration.title";

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeBadURL.class, FakeReferencedURL.class, FakeItem.class, FakeConfigurationProperty.class,
				FakeAccessEntry.class,};
	}

	@Override
	protected List<String> getAddSql(HibernateMigrationHelper helper)
	{
		List<String> sql = helper.getCreationSql(new TablesOnlyFilter("referencedurl", "item_referenced_urls"));
		sql.add(helper.getAddNamedIndex("item_referenced_urls", "iruitemidindex", "item_id"));
		sql.add(helper.getAddNamedIndex("item_referenced_urls", "irurefurlsindex", "referenced_urls_id"));
		return sql;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM BadURL");
	}

	/**
	 * Oracle can't cope with SELECT DISTINCT(my_lob_column), so we retrieve all
	 * URLs from the table and enforce uniqueness post-fact via the hashes.
	 */
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
	{
		// Start by collecting a list of unique Bad URLs. We're not going to
		// have the good URLs until items have been re-saved at some point in
		// the future.
		Date epoch = new Date(0);
		Set<String> hashes = Sets.newHashSet();
		ScrollableResults sr = session.createQuery("SELECT url FROM BadURL WHERE url is NOT NULL").setReadOnly(true)
			.scroll(ScrollMode.FORWARD_ONLY);
		result.incrementStatus();
		try
		{
			while( sr.next() )
			{
				String url = sr.getString(0);
				String hash = DigestUtils.md5Hex(url);
				boolean notPresent = hashes.add(hash);
				if( notPresent )
				{
					FakeReferencedURL fru = new FakeReferencedURL();
					fru.url = url;
					fru.urlHash = hash;
					fru.success = false;
					fru.lastChecked = epoch;
					fru.lastIndexed = epoch;
					session.save(fru);
					result.incrementStatus();
				}
			}
		}
		finally
		{
			sr.close();
		}
		session.flush();

		// Copy the mappings from BadURLs to ReferencedURLs for each item
		sr = session.createQuery("FROM Item WHERE badUrls.size > 0").setReadOnly(true).scroll(ScrollMode.FORWARD_ONLY);
		try
		{
			while( sr.next() )
			{
				FakeItem i = (FakeItem) sr.get(0);
				for( FakeBadURL fbu : i.badUrls )
				{
					if( fbu != null && fbu.url != null )
					{
						String hashedCompareValue = DigestUtils.md5Hex(fbu.url);
						Query query = session.createQuery("FROM ReferencedURL WHERE urlHash = :hashedurl");
						i.referencedUrls.add((FakeReferencedURL) query.setString("hashedurl", hashedCompareValue)
							.uniqueResult());
					}
				}
				session.update(i);
			}
		}
		finally
		{
			sr.close();
		}
		session.flush();

		// Remove old configuration values
		session.createQuery("DELETE  FROM ConfigurationProperty WHERE property like 'urlchecker.%'").executeUpdate();

		// Remove old ACLs
		session.createQuery("DELETE FROM AccessEntry a WHERE a.targetObject = 'C:urlchecking'").executeUpdate();
	}

	@Override
	protected List<String> getDropModifySql(HibernateMigrationHelper helper)
	{
		return helper.getDropTableSql("badurl");
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(migInfo, migInfo);
	}

	@Entity(name = "BadURL")
	@AccessType("field")
	public static class FakeBadURL
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Lob
		String url;
	}

	@Entity(name = "ReferencedURL")
	@AccessType("field")
	@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"urlHash"}))
	public static class FakeReferencedURL
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		// Cannot be Indexed due to SQL server being a fail whale
		@Lob
		String url;

		// Computed MD5 hash of url so SQL Server and Oracle don't cry
		@Column(length = 32)
		@Index(name = "referencedurl_idx")
		String urlHash;

		@NotNull
		boolean success;
		@NotNull
		int status;
		String message;
		@NotNull
		int tries = 0;

		@NotNull
		Date lastChecked;
		@NotNull
		Date lastIndexed;
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@OneToMany
		@JoinColumn(name = "item_key", nullable = false)
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeBadURL> badUrls = new ArrayList<FakeBadURL>();

		@ManyToMany
		@Fetch(value = FetchMode.SUBSELECT)
		List<FakeReferencedURL> referencedUrls = new ArrayList<FakeReferencedURL>();
	}

	@Entity(name = "ConfigurationProperty")
	@AccessType("field")
	public static class FakeConfigurationProperty
	{
		@Id
		String property;
	}

	@Entity(name = "AccessEntry")
	@AccessType("field")
	public static class FakeAccessEntry
	{
		@Id
		long id;

		String targetObject;
	}
}
