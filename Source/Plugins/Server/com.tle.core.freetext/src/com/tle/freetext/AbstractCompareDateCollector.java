package com.tle.freetext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.Institution;

public abstract class AbstractCompareDateCollector extends Collector
{

	private static final Log LOGGER = LogFactory.getLog(AbstractCompareDateCollector.class);

	protected final Map<Long, Institution> instMap;
	protected final List<ItemIndexDelete> toDelete;
	protected IndexReader reader;

	public AbstractCompareDateCollector(Map<Long, Institution> instMap, List<ItemIndexDelete> toDelete)
	{
		this.instMap = instMap;
		this.toDelete = toDelete;
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) throws IOException
	{
		this.reader = reader;
	}

	@Override
	public boolean acceptsDocsOutOfOrder()
	{
		return true;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException
	{
		// don't care
	}

	@SuppressWarnings("nls")
	@Override
	public void collect(int docNum) throws IOException
	{
		Document doc = reader.document(docNum, new MapFieldSelector(FreeTextQuery.FIELD_UNIQUE, FreeTextQuery.FIELD_ID,
			FreeTextQuery.FIELD_INDEXEDTIME, FreeTextQuery.FIELD_INSTITUTION));
		String unique = doc.get(FreeTextQuery.FIELD_UNIQUE);
		long itemId = Long.parseLong(doc.get(FreeTextQuery.FIELD_ID));
		long instId = Long.parseLong(doc.get(FreeTextQuery.FIELD_INSTITUTION));
		String timeStr = doc.get(FreeTextQuery.FIELD_INDEXEDTIME);

		if( unique == null || timeStr == null )
		{
			LOGGER.warn("Corrupt document '" + docNum + "' in index. {unique:" + unique + ", time:" + timeStr + "}");
		}
		else
		{
			compareDate(itemId, instId, Long.parseLong(timeStr));
		}
	}

	public abstract void compareDate(long itemId, long instId, long time);

	public abstract List<IndexedItem> getModifiedDocs();
}
