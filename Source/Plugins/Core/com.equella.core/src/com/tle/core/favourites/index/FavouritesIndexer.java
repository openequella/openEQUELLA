/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.favourites.index;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.beans.item.Bookmark;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.favourites.dao.BookmarkDao;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.core.guice.Bind;
import com.tle.freetext.IndexedItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.lucene.document.Document;

@Bind
@Singleton
public class FavouritesIndexer extends AbstractIndexingExtension {
  @Inject private BookmarkDao dao;

  @Override
  public void indexFast(IndexedItem indexedItem) {
    List<Bookmark> bookmarks = indexedItem.getAttribute(FavouritesIndexer.class);

    if (bookmarks != null) {
      Document doc = indexedItem.getItemdoc();

      Map<String, String> bookmarkFields = new HashMap<>();
      for (Bookmark b : bookmarks) {
        doc.add(indexed(FreeTextQuery.FIELD_BOOKMARK_OWNER, b.getOwner()));
        UtcDate date = new UtcDate(b.getAddedAt());

        String bookmarkDateField = FreeTextQuery.FIELD_BOOKMARK_DATE + b.getOwner();
        String bookmarkDateValue = date.format(Dates.ISO);

        doc.add(indexed(bookmarkDateField, bookmarkDateValue));
        // It's possible that different bookmarks have the same owner so their bookmark date fields
        // are identical.
        // We cannot add duplicated sorting fields to the document. As a result, we add these fields
        // to a map first,
        // and once the for loop is completed, we use the map to add all the fields to the document.
        bookmarkFields.put(bookmarkDateField, bookmarkDateValue);

        for (String tag : b.getKeywords()) {
          doc.add(indexed(FreeTextQuery.FIELD_BOOKMARK_TAGS, tag));
        }
      }

      bookmarkFields.forEach((field, value) -> doc.add(stringSortingField(field, value)));
    }
  }

  @Override
  public void indexSlow(IndexedItem indexedItem) {
    // Nothing
  }

  @Override
  public void loadForIndexing(List<IndexedItem> items) {
    Map<Long, IndexedItem> indexedItems = new HashMap<Long, IndexedItem>();

    for (final IndexedItem indexedItem : items) {
      indexedItems.put(indexedItem.getItemIdKey().getKey(), indexedItem);
    }

    Map<Long, List<Bookmark>> bookmarksForIds = dao.getBookmarksForIds(indexedItems.keySet());

    for (Map.Entry<Long, List<Bookmark>> entry : bookmarksForIds.entrySet()) {
      indexedItems.get(entry.getKey()).setAttribute(FavouritesIndexer.class, entry.getValue());
    }
  }
}
