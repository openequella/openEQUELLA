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
import java.util.stream.Collectors;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.TermQuery;

/**
 * Custom filter to generate a Lucene Boolean query for a list of MUST clauses. Each clause is
 * joined by `Occur.FILTER`. If a clause has multiple criteria, then the criteria are joined by
 * `Occur.SHOULD`.
 * <li>Example 1: Given one clause for two Item statuses, the result is {@code status:live OR
 *     status:archived}.
 * <li>Example 2: Given two clauses for Collection A and two Item statuses, the result is {@code
 *     (collection:A) AND (status:live OR status:archived)}. </li
 */
public class MustFilter implements CustomFilter {

  private final List<List<Field>> clauses;

  public MustFilter(List<List<Field>> clauses) {
    this.clauses = List.copyOf(clauses);
  }

  public BooleanQuery buildQuery() {
    List<List<Field>> nonEmptyClauses = getNonEmptyClauses();

    if (nonEmptyClauses.isEmpty()) {
      return null;
    }

    Builder topLevelBuilder = new Builder();
    nonEmptyClauses.forEach(
        clause -> {
          Builder clauseBuilder = new Builder();
          clause.forEach(
              must ->
                  clauseBuilder.add(
                      new TermQuery(new Term(must.getField(), must.getValue())), Occur.SHOULD));
          topLevelBuilder.add(clauseBuilder.build(), Occur.FILTER);
        });

    return topLevelBuilder.build();
  }

  protected List<List<Field>> getNonEmptyClauses() {
    return clauses.stream().filter(clause -> !clause.isEmpty()).collect(Collectors.toList());
  }
}
