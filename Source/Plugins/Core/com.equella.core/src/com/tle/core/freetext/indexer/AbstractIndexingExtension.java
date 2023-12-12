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

package com.tle.core.freetext.indexer;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.beans.item.ItemSelect;
import com.tle.common.searching.DateFilter;
import com.tle.common.searching.DateFilter.Format;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;

public abstract class AbstractIndexingExtension implements IndexingExtension {
  @Override
  public void prepareForLoad(ItemSelect select) {
    // nothing by default
  }

  public void addAllFields(Document doc, List<Field> fields) {
    for (Field field : fields) {
      doc.add(field);
    }
  }

  protected static String getSortableDate(Date date, DateFilter.Format format, Long defaultTime) {
    return new UtcDate(date).format(Dates.ISO);
  }

  public static void addDateField(Document doc, String name, Date date, DateFilter.Format format) {
    String val;
    long time = Long.MAX_VALUE;
    if (format == Format.ISO) {
      if (date == null) {
        date = new Date(time);
      }
      val = new UtcDate(date).format(Dates.ISO);
    } else {
      if (date != null) {
        time = date.getTime();
        val = Long.toString(time);
      } else {
        val = Long.toString(time);
      }
    }
    doc.add(numericSortingField(name, time));
    doc.add(new StringField(name, val, Field.Store.NO));
  }

  public static Field stringSortingField(String field, String value) {
    return new SortedDocValuesField(field, new BytesRef(value));
  }

  public static Field numericSortingField(String field, long value) {
    return new NumericDocValuesField(field, value);
  }

  public static Field indexed(String name, String value) {
    FieldType ft = new FieldType();
    ft.setStored(false);
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    ft.setTokenized(false);

    return new Field(name, value, ft);
  }

  public static Field keyword(String name, String value) {
    FieldType ft = new FieldType();
    ft.setStored(true);
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    ft.setTokenized(false);

    return new Field(name, value, ft);
  }

  public static Field keyword(String name, TokenStream value) {
    return new TextField(name, value);
  }

  public static Field unstored(String name, String value) {
    FieldType ft = new FieldType();
    ft.setStored(false);
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    ft.setTokenized(true);

    return new Field(name, value, ft);
  }

  public static Field unstoredAndVectored(String name, String value) {
    FieldType ft = new FieldType();
    ft.setStored(false);
    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    ft.setTokenized(true);
    ft.setStoreTermVectors(true);

    return new Field(name, value, ft);
  }

  protected StringBuilder gatherLanguageBundles(LanguageBundle... bundles) {
    StringBuilder builder = new StringBuilder();
    for (LanguageBundle bundle : bundles) {
      if (bundle != null) {
        Map<String, LanguageString> strings = bundle.getStrings();
        if (strings != null) {
          for (LanguageString langstring : bundle.getStrings().values()) {
            builder.append(langstring.getText());
            builder.append(' ');
          }
        }
      }
    }
    return builder;
  }
}
