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

package com.tle.core.notification.standard.indexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.lucene.document.Document;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.freetext.index.MultipleIndex;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class NotificationIndex extends MultipleIndex<NotificationResult>
{
	public static final String INDEXID = "notifications"; //$NON-NLS-1$
	public static final String FIELD_ID = "note_id"; //$NON-NLS-1$
	public static final String FIELD_USER = "note_user"; //$NON-NLS-1$
	public static final String FIELD_REASON = "note_reason"; //$NON-NLS-1$
	public static final String FIELD_DATE = "note_date"; //$NON-NLS-1$

	@Override
	public String getIndexId()
	{
		return INDEXID;
	}

	@Override
	protected Set<String> getKeyFields()
	{
		return new HashSet<String>(Arrays.asList(FreeTextQuery.FIELD_UNIQUE, FreeTextQuery.FIELD_ID, FIELD_ID));
	}

	@Override
	protected NotificationResult createResult(ItemIdKey key, Document doc, float relevance, boolean sortByRelevance)
	{
		return new NotificationResult(key, Long.parseLong(doc.get(FIELD_ID)), relevance, sortByRelevance);
	}

}
