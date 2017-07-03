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

package com.tle.core.wizard.controls.universal.migration.v50;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import javax.persistence.OneToOne;

import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.hibernate.classic.Session;
import org.w3c.dom.Node;

import com.dytech.edge.common.Constants;
import com.google.common.collect.ImmutableMap;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemXml;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.xml.XmlDocument;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class ReplaceDeletedControlsItemDatabaseMigration extends AbstractHibernateDataMigration
{
	// We don't handle ItemAttachments, they already have an xpath value that
	// just needs populating
	private static final Map<String, String> DESCRIMINATOR_TO_CLASS = new ImmutableMap.Builder<String, String>()
		.put("attachment", "com.tle.beans.item.attachments.LinkAttachment")
		.put("html", "com.tle.beans.item.attachments.HtmlAttachment")
		.put("file", "com.tle.beans.item.attachments.FileAttachment")
		.put("zip", "com.tle.beans.item.attachments.ZipAttachment")
		.put("ims", "com.tle.beans.item.attachments.ImsAttachment")
		.put("imsres", "com.tle.beans.item.attachments.IMSResourceAttachment")
		.put("custom", "com.tle.beans.item.attachments.CustomAttachment").build();

	@Override
	public boolean isBackwardsCompatible()
	{
		return true;
	}

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo("com.tle.core.wizard.controls.universal.migration.associateattachments.title",
			Constants.BLANK);
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		ScrollableResults iter = session.createQuery("from Item").scroll();
		while( iter.next() )
		{
			FakeItem item = (FakeItem) iter.get()[0];
			final ItemXml itemXml = item.getItemXml();
			final XmlDocument xml = new XmlDocument(itemXml.getXml());
			final Map<ItemId, String> itemAttachments = new HashMap<ItemId, String>();
			boolean modified = false;

			Iterator<FakeAttachment> attiter = item.getAttachments().iterator();
			while( attiter.hasNext() )
			{
				FakeAttachment attachment = attiter.next();

				if( attachment.type.equals("file") && attachment.url.startsWith("_zips/") )
				{
					attiter.remove();
					continue;
				}
				else if( attachment.type.equals("zip") && !attachment.url.startsWith("_zips/") )
				{
					attachment.description = attachment.url;
					attachment.url = "_zips/" + attachment.url;
				}
				else if( attachment.type.equals("item") )
				{
					final ItemId itemId = new ItemId(attachment.value1);
					itemAttachments.put(itemId, attachment.uuid);

					// convert to resource attachment:
					attachment.type = "custom";
					attachment.value1 = "resource";
					attachment.url = "";
					attachment.setData("uuid", itemId.getUuid());
					attachment.setData("version", itemId.getVersion());
					attachment.setData("type", "p");
					continue;
				}

				final String uuid = attachment.uuid;
				String classname = DESCRIMINATOR_TO_CLASS.get(attachment.type);
				final String handler = ReplaceDeletedControlsItemXmlMigration.getHandler(classname, attachment.value1);
				if( handler == null )
				{
					throw new RuntimeException(
						"Unhandled attachment type " + attachment.getClass().getName() + "(attachment value - "
							+ (attachment.value1 != null ? attachment.value1 : "unspecified") + ')');
				}

				if( !Check.isEmpty(handler) )
				{
					final String xpath = ReplaceDeletedControlsXmlMigration.getXpathForHandler(handler);
					if( !xml.nodeValues("xml/" + xpath, null).contains(uuid) )
					{
						xml.createNodeFromXPath("xml/" + xpath, uuid);
						modified = true;
					}
				}
			}

			if( !itemAttachments.isEmpty() )
			{
				for( Entry<ItemId, String> entry : itemAttachments.entrySet() )
				{
					final String uuid = entry.getValue();
					final ItemId itemId = entry.getKey();

					// There must be an item finder control. We need to look for
					// ALL nodes with
					// [@uuid=UUID and @version=version] and add the attachment
					// UUID underneath them
					for( Node node : xml
						.nodeList("//*[@uuid='" + itemId.getUuid() + "' and @version='" + itemId.getVersion() + "']") )
					{
						// Don't modify attachment nodes! (actually, these
						// probably don't exist in ItemXml anyway)
						if( !node.getNodeName().equals("attachment") )
						{
							node.setTextContent(uuid);
							modified = true;
						}
					}
				}
			}

			if( modified )
			{
				itemXml.setXml(xml.toString());
				session.save(itemXml);
			}
			session.flush();
			session.clear();
			result.incrementStatus();
		}
	}

	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		return count(session, "from Item");
	}

	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class<?>[]{FakeItem.class, FakeAttachment.class, ItemXml.class};
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

		@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
		@IndexColumn(name = "attindex")
		@Fetch(value = FetchMode.SUBSELECT)
		@JoinColumn(name = "item_id", nullable = false)
		private List<FakeAttachment> attachments = new ArrayList<FakeAttachment>();

		@Index(name = "itemItemXmlIndex")
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
		@JoinColumn(unique = true, nullable = false)
		@XStreamOmitField
		private ItemXml itemXml;

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

		public ItemXml getItemXml()
		{
			return itemXml;
		}

		public void setItemXml(ItemXml itemXml)
		{
			this.itemXml = itemXml;
		}
	}
}
