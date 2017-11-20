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

package com.tle.core.freetext.index;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.NRTManager;

import com.tle.core.services.item.FreetextResult;
import com.tle.freetext.FreetextIndex;
import com.tle.freetext.IndexedItem;

public abstract class MultipleIndex<T extends FreetextResult> extends ItemIndex<T>
{
	@Inject
	private FreetextIndex freetextIndex;

	public abstract String getIndexId();

	@PostConstruct
	@Override
	public void afterPropertiesSet() throws IOException
	{
		setIndexPath(new File(freetextIndex.getRootIndexPath(), "index_" + getIndexId())); //$NON-NLS-1$
		super.afterPropertiesSet();
	}

	@Override
	public long addDocuments(Collection<IndexedItem> documents, NRTManager nrtManager)
	{
		long generation = -1;
		for( IndexedItem item : documents )
		{
			if( item.isAdd() )
			{
				List<Document> docs = getDocuments(item);
				if( docs != null )
				{
					for( Document doc : docs )
					{
						try
						{
							long g = nrtManager.addDocument(doc);
							if( item.isNewSearcherRequired() )
							{
								generation = g;
							}
						}
						catch( IOException e )
						{
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
		return generation;
	}

	public final List<Document> getDocuments(IndexedItem item)
	{
		return item.getDocumentsForIndex(getIndexId());
	}
}
