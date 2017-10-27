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

package com.tle.core.activation.index;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.activation.service.ActivationService;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.core.guice.Bind;
import com.tle.core.institution.RunAsInstitution;
import com.tle.freetext.IndexedItem;

@Bind
@Singleton
public class ActivationIndexer extends AbstractIndexingExtension
{
	@Inject
	private ActivationService activationService;
	@Inject
	private RunAsInstitution runAs;

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		// nothing
	}

	@Override
	public void loadForIndexing(final List<IndexedItem> items)
	{
		for( final IndexedItem indexedItem : items )
		{
			runAs.executeAsSystem(indexedItem.getInstitution(), new Callable<Void>()
			{
				@Override
				public Void call()
				{
					indexedItem.prepareACLEntries(indexedItem.getItem(), ActivationConstants.VIEW_ACTIVATION_ITEM,
						ActivationConstants.VIEW_ACTIVATION_ITEM_PFX);
					return null;
				}
			});
		}
	}

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		Item item = indexedItem.getItem();
		List<ActivateRequest> activateRequests = activationService.getAllRequests(item);

		Document itemdoc = indexedItem.getItemdoc();
		List<Fieldable> basicFields = indexedItem.getBasicFields();
		List<Document> activationDocs = indexedItem.getDocumentsForIndex(ActivationConstants.ACTIVATION_INDEX_ID);
		for( ActivateRequest request : activateRequests )
		{
			if( request.getCourse() != null )
			{
				itemdoc.add(keyword(FreeTextQuery.FIELD_COURSE_ID, request.getCourse().getUuid()));

				Document doc = new Document();
				addAllFields(doc, basicFields);
				doc.add(keyword(FreeTextQuery.FIELD_COURSE_ID, request.getCourse().getUuid()));
				doc.add(keyword(FreeTextQuery.FIELD_ACTIVATION_ID, Long.toString(request.getId())));
				doc.add(keyword(FreeTextQuery.FIELD_ACTIVATION_STATUS, String.valueOf(request.getStatus())));
				doc.add(indexed(FreeTextQuery.FIELD_OWNER, request.getUser()));
				doc.removeField(FreeTextQuery.FIELD_REALLASTMODIFIED);
				doc.add(keyword(FreeTextQuery.FIELD_REALLASTMODIFIED, new UtcDate(request.getTime()).format(Dates.ISO)));

				doc.add(indexed(FreeTextQuery.FIELD_ACTIVATION_FROM, new UtcDate(request.getFrom()).format(Dates.ISO)));
				doc.add(indexed(FreeTextQuery.FIELD_ACTIVATION_UNTIL, new UtcDate(request.getUntil()).format(Dates.ISO)));

				addAllFields(doc, indexedItem.getACLEntries(ActivationConstants.VIEW_ACTIVATION_ITEM));
				activationDocs.add(doc);
			}
		}
	}
}
