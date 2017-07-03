/*
 * Created on Aug 31, 2005
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
