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

package com.tle.core.freetext.filters;

import com.tle.common.searching.Field;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.FixedBitSet;

public class MatrixFilter implements CustomFilter {

  private List<Field> fields;

  private IndexReader reader;

  public MatrixFilter(List<Field> matrixFields, IndexReader reader) {
    this.fields = matrixFields;
    this.reader = reader;
  }

  @Override
  public Query buildQuery() {
    Builder builder = new Builder();

    Map<String, Map<String, FixedBitSet>> xpathMap =
        new HashMap<String, Map<String, FixedBitSet>>();
    for (Field fieldObj : fields) {
      String field = fieldObj.getField();
      try {
        for (Term term : new XPathFieldIterator(reader, field, "")) {
          if (term.text().equals(fieldObj.getValue())) {
            builder.add(new TermQuery(term), Occur.SHOULD);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return builder.build();
  }
}
