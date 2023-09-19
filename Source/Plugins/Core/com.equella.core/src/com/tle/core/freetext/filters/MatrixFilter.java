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
import java.util.List;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Custom filter to generate a Lucene Boolean query for a list of matrix fields. This one is
 * typically used together with Schema nodes.
 */
public class MatrixFilter implements CustomFilter {

  private final List<Field> fields;

  private final IndexReader reader;

  public MatrixFilter(List<Field> matrixFields, IndexReader reader) {
    this.fields = List.copyOf(matrixFields);
    this.reader = reader;
  }

  @Override
  public Query buildQuery() {
    Builder builder = new Builder();

    for (Field fieldObj : fields) {
      try {
        for (Term term : new XPathFieldIterator(reader, fieldObj.getField())) {
          if (term.text().equals(fieldObj.getValue())) {
            builder.add(new TermQuery(term), Occur.SHOULD);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException("Failed to build Lucene Query for MatrixFilter", e);
      }
    }

    return builder.build();
  }
}
