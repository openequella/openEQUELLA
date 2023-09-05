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
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.TermQuery;

public class MustNotFilter extends MustFilter {
  public MustNotFilter(List<List<Field>> mustNotClauses) {
    super(mustNotClauses);
  }

  /**
   * Build a BooleanQuery from the Must Not clauses. All the criteria are joint by `Occur.SHOULD`,
   * but the whole query is negated by `Occur.MUST_NOT`.
   *
   * <p>For example, given two clauses for Collection and Item status, the result is '-(collection:A
   * status:live status:archived)'.
   */
  public BooleanQuery buildQuery() {
    List<List<Field>> nonEmptyClauses = getNonEmptyClauses();

    if (nonEmptyClauses.isEmpty()) {
      return null;
    }

    Builder builder = new Builder();
    nonEmptyClauses.forEach(
        clause ->
            clause.forEach(
                mustNot ->
                    builder.add(
                        new TermQuery(new Term(mustNot.getField(), mustNot.getValue())),
                        Occur.SHOULD)));

    return builder.build();
  }

  @Override
  public Occur getOccur() {
    return Occur.MUST_NOT;
  }
}
