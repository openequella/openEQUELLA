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

package com.tle.core.freetext.index;

import com.tle.common.searching.Field;
import com.tle.core.freetext.filters.XPathFieldIterator;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.OpenBitSet;

public class MatrixFilter extends Filter {

  private List<Field> fields;

  public MatrixFilter(List<Field> matrixFields) {
    this.fields = matrixFields;
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
    AtomicReader reader = context.reader();
    int maxDoc = reader.maxDoc();
    Map<String, Map<String, OpenBitSet>> xpathMap = new HashMap<String, Map<String, OpenBitSet>>();
    for (Field fieldObj : fields) {
      String field = fieldObj.getField();
      boolean hasXpaths = field.indexOf('[') != -1;
      Set<String> mustHave = new HashSet<String>();
      for (Term term : new XPathFieldIterator(reader, field, "")) // $NON-NLS-1$
      {
        if (term.text().equals(fieldObj.getValue())) {
          OpenBitSet set = new OpenBitSet(maxDoc);
          LuceneDocumentHelper.forEachDoc(reader, term, set::set);
          String xpathKey = "";
          if (hasXpaths) {
            String fieldName = term.field();
            int ind = fieldName.lastIndexOf(']');
            if (ind != -1) {
              xpathKey = fieldName.substring(0, ind + 1);
            }
          }
          mustHave.add(xpathKey);
          Map<String, OpenBitSet> bitSetMap = xpathMap.get(xpathKey);
          if (bitSetMap == null) {
            bitSetMap = new HashMap<String, OpenBitSet>();
            xpathMap.put(xpathKey, bitSetMap);
          }
          bitSetMap.put(field, set);
        }
      }
    }
    OpenBitSet retSet = null;
    for (Map<String, OpenBitSet> bitSetMap : xpathMap.values()) {
      OpenBitSet set = null;
      for (Field fieldObj : fields) {
        String field = fieldObj.getField();
        OpenBitSet thisSet = bitSetMap.get(field);
        if (thisSet == null) {
          set = null;
          break;
        } else if (set == null) {
          set = thisSet;
        } else {
          set.and(thisSet);
        }
      }
      if (retSet == null) {
        retSet = set;
      } else if (set != null) {
        retSet.or(set);
      }
    }
    return retSet == null ? new OpenBitSet(maxDoc) : retSet;
  }
}
