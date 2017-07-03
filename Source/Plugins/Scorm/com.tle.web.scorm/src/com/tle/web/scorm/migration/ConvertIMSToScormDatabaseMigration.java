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

package com.tle.web.scorm.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;

import com.dytech.edge.common.Constants;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.services.FileSystemService;

@Bind
@Singleton
public class ConvertIMSToScormDatabaseMigration extends AbstractHibernateDataMigration
{
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.web.scorm.migration.title", Constants.BLANK); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		ScrollableResults iter = session.createQuery("FROM Item i LEFT JOIN i.attachments a WHERE a.type = 'ims'")
			.scroll();
		while( iter.next() )
		{
			FakeItem item = (FakeItem) iter.get()[0];

			List<FakeAttachment> attachments = item.getAttachments();
			Iterator<FakeAttachment> attiter = attachments.iterator();
			boolean modified = false;
			while( attiter.hasNext() )
			{
				FakeAttachment attachment = attiter.next();
				if( attachment.type.equals("ims") )
				{
					if( !Check.isEmpty(attachment.value2) )
					{
						modified = true;
						attachment.type = "custom";
						String size = attachment.value1;
						if( !Check.isEmpty(size) )
						{
							attachment.setData("fileSize", Long.parseLong(size));
						}
						attachment.setData("SCORM_VERSION", attachment.value2);
						attachment.value1 = "scorm";
						attachment.value2 = null;
						break;
					}
				}
			}
			if( modified )
			{
				attiter = attachments.iterator();
				while( attiter.hasNext() )
				{
					FakeAttachment attachment = attiter.next();
					if( attachment.type.equals("imsres") )
					{
						attachment.type = "custom";
						attachment.value1 = "scormres";
					}
				}

				ItemFile itemFile = new ItemFile(new ItemId(item.uuid, item.version), null);
				Institution inst = new Institution();
				inst.setFilestoreId(item.institution.getFilestoreId());
				itemFile.setInstitution(inst);
				fileSystemService.rename(itemFile, "_IMS", "_SCORM");

				session.flush();
				session.clear();
			}
			result.incrementStatus();
		}

	}

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "FROM Item i LEFT JOIN i.attachments a WHERE a.type = 'ims'"); //$NON-NLS-1$
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItem.class, FakeAttachment.class, Institution.class};
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(length = 40, nullable = false)
		@Index(name = "attachmentUuidIndex")
		String uuid;

		@ManyToOne
		@JoinColumn(name = "item_id", insertable = false, updatable = false, nullable = false)
		@XStreamOmitField
		@Index(name = "attachmentItem")
		FakeItem item;

		@Column(length = 31)
		String type;

		@Column(length = 1024)
		String value1;

		@Column(length = 1024)
		String value2;

		@Column(length = 1024)
		String url;

		@Column(length = 1024)
		String description;

		@Type(type = "xstream_immutable")
		@Column(length = 8192)
		Map<String, Object> data;
		private transient boolean dataModified;

		public void setData(String name, Object value)
		{
			if( data == null )
			{
				data = new HashMap<String, Object>();
			}
			else if( !dataModified )
			{
				data = new HashMap<String, Object>(data);
			}
			dataModified = true;
			data.put(name, value);
		}

		public Object getData(String name)
		{
			return data == null ? null : data.get(name);
		}
	}

	@Entity(name = "Item")
	@AccessType("field")
	public static class FakeItem
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private long id;

		@Index(name = "itemUuidIndex")
		@Column(length = 40)
		private String uuid;

		@Index(name = "itemVersionIndex")
		private int version;

		@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
		@IndexColumn(name = "attindex")
		@Fetch(value = FetchMode.SUBSELECT)
		@JoinColumn(name = "item_id", nullable = false)
		private List<FakeAttachment> attachments = new ArrayList<FakeAttachment>();

		// An unread private field is forgivable: it's a fake item after all
		@JoinColumn(nullable = false)
		@ManyToOne(fetch = FetchType.LAZY)
		@Index(name = "itemInstitutionIndex")
		@XStreamOmitField
		private Institution institution; // NOSONAR

		public long getId()
		{
			return id;
		}

		public void setId(long id)
		{
			this.id = id;
		}

		public List<FakeAttachment> getAttachments()
		{
			return attachments;
		}

		public void setAttachments(List<FakeAttachment> attachments)
		{
			this.attachments = attachments;
		}

	}
}
