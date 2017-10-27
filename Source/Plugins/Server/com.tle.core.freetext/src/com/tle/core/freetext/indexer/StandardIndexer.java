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

package com.tle.core.freetext.indexer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.hibernate.Hibernate;
import org.hibernate.type.SerializationException;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.ReferencedURL;
import com.tle.beans.entity.Schema;
import com.tle.beans.item.Comment;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.freetext.FreetextIndex;
import com.tle.freetext.IndexedItem;
import com.tle.freetext.TextExtracter;
import com.tle.freetext.XmlSchemaIndexer;

@Bind
@Singleton
public class StandardIndexer extends AbstractIndexingExtension
{
	private static String DISCOVER_ITEM = "DISCOVER_ITEM"; //$NON-NLS-1$
	private static String DELETE_ITEM = "DELETE_ITEM"; //$NON-NLS-1$
	private static String DOWNLOAD_ITEM = "DOWNLOAD_ITEM"; //$NON-NLS-1$
	private static String VIEW_ITEM = "VIEW_ITEM"; //$NON-NLS-1$

	private static final String TYPE_RESOURCE = "resource"; //$NON-NLS-1$
	private static final String DATA_UUID = "uuid"; //$NON-NLS-1$
	private static final String DATA_VERSION = "version"; //$NON-NLS-1$

	private static NumberFormat FORMAT = new DecimalFormat("0.00"); //$NON-NLS-1$
	private static Logger LOGGER = Logger.getLogger(StandardIndexer.class);

	@Inject
	private TextExtracter textExtracter;
	@Inject
	private FreetextIndex freetextIndex;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		Document itemdoc = indexedItem.getItemdoc();

		addAllFields(itemdoc, indexedItem.getBasicFields());

		// Finish processing the body string.
		PropBagEx itemxml = indexedItem.getItemxml();

		// Start adding the fields
		if( itemxml.nodeExists(FreeTextQuery.FIELD_DOWNLOAD_URL) )
		{
			itemdoc.add(indexed(FreeTextQuery.FIELD_DOWNLOAD_URL, itemxml.getNode(FreeTextQuery.FIELD_DOWNLOAD_URL)));
		}

		addAllFields(itemdoc, indexedItem.getACLEntries(DELETE_ITEM));
		addAllFields(itemdoc, indexedItem.getACLEntries(DISCOVER_ITEM));
		addAllFields(itemdoc, indexedItem.getACLEntries(DOWNLOAD_ITEM));
		addAllFields(itemdoc, indexedItem.getACLEntries(VIEW_ITEM));

		List<ReferencedURL> referencedUrls = indexedItem.getItem().getReferencedUrls();
		for( ReferencedURL url : referencedUrls )
		{
			if( !url.isSuccess() )
			{
				itemdoc.add(keyword(FreeTextQuery.FIELD_IS_BAD_URL, "true"));
				break;
			}

		}

	}

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		List<Fieldable> fields = textExtracter.indexAttachments(indexedItem, freetextIndex.getSearchSettings());
		addAllFields(indexedItem.getItemdoc(), fields);
	}

	@Override
	public void loadForIndexing(List<IndexedItem> items)
	{
		for( final IndexedItem indexedItem : items )
		{
			runAs.executeAsSystem(indexedItem.getInstitution(), new Callable<Void>()
			{
				@Override
				public Void call()
				{
					indexedItem.prepareACLEntries(indexedItem, DELETE_ITEM);
					indexedItem.prepareACLEntries(indexedItem, DISCOVER_ITEM);
					indexedItem.prepareACLEntries(indexedItem, DOWNLOAD_ITEM);
					indexedItem.prepareACLEntries(indexedItem, VIEW_ITEM);
					Item item = indexedItem.getItem();
					initBundle(item.getName());
					initBundle(item.getDescription());
					Hibernate.initialize(item.getInstitution());
					Hibernate.initialize(item.getComments());
					Hibernate.initialize(item.getItemDefinition());
					Hibernate.initialize(item.getItemDefinition().getSchema());
					Hibernate.initialize(item.getItemDefinition().getWorkflow());
					return null;
				}
			});
		}
	}

	@Override
	public void prepareForLoad(ItemSelect select)
	{
		select.setAttachments(true);
		select.setNotifications(true);
		select.setModeration(true);
		select.setCollaborators(true);
		select.setModerationStatuses(true);
	}

	@SuppressWarnings("nls")
	public List<Fieldable> getBasicFields(IndexedItem indexedItem)
	{
		ItemIdKey id = indexedItem.getItemIdKey();
		Item item = indexedItem.getItem();
		long instId = item.getInstitution().getUniqueId();
		List<Fieldable> fields = new ArrayList<Fieldable>();
		String szName = gatherLanguageBundles(item.getName()).toString().toLowerCase().trim();
		ItemStatus itemstatus = item.getStatus();

		String itemID = id.getUuid();
		String sVersion = Integer.toString(id.getVersion());

		UtcDate dateModifiedDate = new UtcDate(item.getDateModified());
		UtcDate dateCreatedDate = new UtcDate(item.getDateCreated());
		Date lastIndexed = item.getDateForIndex();
		Date liveApprovalDate = (item.getModeration() == null ? null : item.getModeration().getLiveApprovalDate());

		// We want to get the date modified as an integer such as YYYYMMDD.
		// We strip of the hours:minutes:seconds as we don't care about that.
		String realLastModified = dateModifiedDate.format(Dates.ISO);
		String realCreated = dateCreatedDate.format(Dates.ISO);

		String rating = FORMAT.format(item.getRating());

		fields.add(keyword(FreeTextQuery.FIELD_INSTITUTION, Long.toString(instId)));
		fields.add(keyword(FreeTextQuery.FIELD_RATING, rating));
		fields.add(keyword(FreeTextQuery.FIELD_UUID, itemID));
		fields.add(keyword(FreeTextQuery.FIELD_VERSION, sVersion));
		fields.add(keyword(FreeTextQuery.FIELD_UNIQUE, item.getIdString()));
		fields.add(keyword(FreeTextQuery.FIELD_ID, Long.toString(item.getId())));
		fields.add(new NumericField(FreeTextQuery.FIELD_ID_RANGEABLE, 1, Store.YES, true).setLongValue(item.getId()));
		fields.add(keyword(FreeTextQuery.FIELD_INDEXEDTIME, Long.toString(lastIndexed.getTime())));
		fields.add(keyword(FreeTextQuery.FIELD_ITEMDEFID, item.getItemDefinition().getUuid()));

		Schema schema = item.getItemDefinition().getSchema();
		if( schema != null )
		{
			fields.add(keyword(FreeTextQuery.FIELD_SCHEMAID, schema.getUuid()));
		}

		fields.add(keyword(FreeTextQuery.FIELD_REALLASTMODIFIED, realLastModified));
		fields.add(keyword(FreeTextQuery.FIELD_REALCREATED, realCreated));
		if( liveApprovalDate != null )
		{
			fields
				.add(indexed(FreeTextQuery.FIELD_LIVE_APPROVAL_DATE, new UtcDate(liveApprovalDate).format(Dates.ISO)));
		}
		fields.add(keyword(FreeTextQuery.FIELD_ITEMSTATUS, itemstatus.toString()));
		fields.add(keyword(FreeTextQuery.FIELD_NAME, szName));
		addName(fields, szName);
		fields.add(unstoredAndVectored(FreeTextQuery.FIELD_NAME_AUTOCOMPLETE, szName));
		fields.add(keyword(FreeTextQuery.FIELD_MODERATING, Boolean.toString(item.isModerating())));
		fields.add(unstored(FreeTextQuery.FIELD_ALL, "1")); //$NON-NLS-1$

		UnmodifiableAttachments attachments = new UnmodifiableAttachments(item);
		ImsAttachment ims = attachments.getIms();
		if( ims != null )
		{
			String packfile = ims.getUrl();
			fields.add(indexed(FreeTextQuery.FIELD_PACKAGEFILE, packfile));
			fields.add(indexed("all", FreeTextQuery.FIELD_PACKAGEFILE)); //$NON-NLS-1$
		}

		List<CustomAttachment> custAttachments = attachments.getCustomList(TYPE_RESOURCE);
		for( CustomAttachment attachment : custAttachments )
		{
			fields.add(keyword(FreeTextQuery.FIELD_ATTACHMENT_UUID, (String) attachment.getData(DATA_UUID)));
			fields.add(keyword(FreeTextQuery.FIELD_ATTACHMENT_UUID_VERSION, (String) attachment.getData(DATA_UUID)
				+ "." + attachment.getData(DATA_VERSION))); //$NON-NLS-1$
		}

		StringBuilder bodyTextBuf = gatherLanguageBundles(item.getDescription());
		if( !Check.isEmpty(item.getComments()) )
		{
			for( Comment comment : item.getComments() )
			{
				if( !Check.isEmpty(comment.getComment()) )
				{
					bodyTextBuf.append(comment.getComment());
					bodyTextBuf.append(' ');
				}
			}
		}
		addBody(fields, bodyTextBuf.toString());
		fields.addAll(indexFields(schema, indexedItem.getItemxml()));

		fields.add(indexed(FreeTextQuery.FIELD_OWNER, item.getOwner()));
		if( item.getCollaborators() != null )
		{
			for( String collab : item.getCollaborators() )
			{
				fields.add(indexed(FreeTextQuery.FIELD_OWNER, collab));
			}
		}

		return fields;
	}

	private void addName(List<Fieldable> fields, String name)
	{
		fields.add(unstoredAndVectored(FreeTextQuery.FIELD_NAME_VECTORED, name));
		fields.add(unstoredAndVectored(FreeTextQuery.FIELD_NAME_VECTORED_NOSTEM, name));
	}

	private void addBody(List<Fieldable> fields, String body)
	{
		fields.add(unstoredAndVectored(FreeTextQuery.FIELD_BODY, body));
		fields.add(unstoredAndVectored(FreeTextQuery.FIELD_BODY_NOSTEM, body));
	}

	private List<Field> indexFields(Schema schema, PropBagEx resBag)
	{
		List<Field> fields = new ArrayList<Field>();
		if( schema != null )
		{
			try
			{
				XmlSchemaIndexer schemaIndexer = new XmlSchemaIndexer();
				schemaIndexer.indexChildNodes(schema.getRootSchemaNode(), schema.getItemNamePath(), "", "", resBag); //$NON-NLS-1$ //$NON-NLS-2$
				fields.addAll(schemaIndexer.getIndexedFields());
				Set<String> allPaths = schemaIndexer.getPathsIndexed();
				for( String path : allPaths )
				{
					fields.add(indexed(FreeTextQuery.FIELD_ALL, path));
				}
				Map<String, StringBuilder> pathValuesMap = schemaIndexer.getPathValuesMap();
				for( Map.Entry<String, StringBuilder> entry : pathValuesMap.entrySet() )
				{
					fields.add(unstored(entry.getKey() + '*', entry.getValue().toString()));
				}
			}
			catch( SerializationException ex )
			{
				LOGGER.error("Schema causing this issue is " + schema.getId() + " - " //$NON-NLS-1$ //$NON-NLS-2$
					+ schema.getUuid(), ex);
				throw ex;
			}
		}
		return fields;
	}
}
