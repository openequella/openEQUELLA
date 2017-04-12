package com.tle.core.notification.indexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.lucene.document.Document;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.guice.Bind;
import com.tle.freetext.index.MultipleIndex;

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
