package com.tle.core.favourites.index;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.lucene.document.Document;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.Bookmark;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.favourites.dao.BookmarkDao;
import com.tle.core.guice.Bind;
import com.tle.freetext.AbstractIndexingExtension;
import com.tle.freetext.IndexedItem;

@Bind
@Singleton
public class FavouritesIndexer extends AbstractIndexingExtension
{
	@Inject
	private BookmarkDao dao;

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		List<Bookmark> bookmarks = indexedItem.getAttribute(FavouritesIndexer.class);

		if( bookmarks != null )
		{
			Document doc = indexedItem.getItemdoc();

			for( Bookmark b : bookmarks )
			{
				doc.add(indexed(FreeTextQuery.FIELD_BOOKMARK_OWNER, b.getOwner()));
				UtcDate date = new UtcDate(b.getDateModified());
				doc.add(indexed(FreeTextQuery.FIELD_BOOKMARK_DATE + b.getOwner(), date.format(Dates.ISO)));
				for( String tag : b.getKeywords() )
				{
					doc.add(indexed(FreeTextQuery.FIELD_BOOKMARK_TAGS, tag));
				}
			}
		}
	}

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		// Nothing
	}

	@Override
	public void loadForIndexing(List<IndexedItem> items)
	{
		Map<Long, IndexedItem> indexedItems = new HashMap<Long, IndexedItem>();

		for( final IndexedItem indexedItem : items )
		{
			indexedItems.put(indexedItem.getItemIdKey().getKey(), indexedItem);
		}

		Map<Long, List<Bookmark>> bookmarksForIds = dao.getBookmarksForIds(indexedItems.keySet());

		for( Map.Entry<Long, List<Bookmark>> entry : bookmarksForIds.entrySet() )
		{
			indexedItems.get(entry.getKey()).setAttribute(FavouritesIndexer.class, entry.getValue());
		}
	}
}
