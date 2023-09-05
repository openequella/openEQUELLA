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

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FixedBitSet;

public class SecurityFilter implements CustomFilter {

  private FixedBitSet results;

  private String[] expressions;
  private Map<String, Boolean> ownerExprMap;
  private int ownerSizes;
  private boolean systemUser;

  private IndexReader reader;

  public SecurityFilter(String aclType, IndexReader reader) {
    this.reader = reader;
    ownerExprMap = new HashMap<String, Boolean>();

    UserState userState = CurrentUser.getUserState();
    systemUser = userState.isSystem();
    Collection<Long> aclExpressions = userState.getCommonAclExpressions();
    Collection<Long> ownerAclExpressions = userState.getOwnerAclExpressions();
    Collection<Long> notOwnerAclExpressions = userState.getNotOwnerAclExpressions();

    ownerSizes =
        (ownerAclExpressions == null ? 0 : ownerAclExpressions.size())
            + (notOwnerAclExpressions == null ? 0 : notOwnerAclExpressions.size());
    expressions = new String[(aclExpressions == null ? 0 : aclExpressions.size()) + ownerSizes];

    int i = 0;
    if (aclExpressions != null) {
      for (Long acl : aclExpressions) {
        expressions[i++] = aclType + acl;
      }
    }

    if (ownerAclExpressions != null) {
      for (Long acl : ownerAclExpressions) {
        String aclStr = aclType + acl;
        expressions[i++] = aclStr;
        ownerExprMap.put(aclStr, true);
      }
    }
    if (notOwnerAclExpressions != null) {
      for (Long acl : notOwnerAclExpressions) {
        String aclStr = aclType + acl;
        expressions[i++] = aclStr;
        ownerExprMap.put(aclStr, false);
      }
    }
  }

  private Set<Term> getTermsForField(String field) {
    Set<Term> set = new HashSet<>();
    try {
      Terms terms = MultiFields.getTerms(reader, field);
      if (terms != null) {
        TermsEnum termsEnum = terms.iterator();
        while (termsEnum.next() != null) {
          set.add(new Term(field, new BytesRef(termsEnum.term().utf8ToString())));
        }
      }

      return set;
    } catch (IOException e) {
      throw new RuntimeException("Failed to list terms for field " + field, e);
    }
  }

  @Override
  public Query buildQuery() {
    Builder builder = new Builder();

    if (systemUser) {
      return null;
    }

    if (ownerSizes > 0) {
      builder.add(
          new TermQuery(new Term(FreeTextQuery.FIELD_OWNER, CurrentUser.getUserID())),
          Occur.SHOULD);
    }

    Set<Term> allTerms = new TreeSet<>();

    Arrays.stream(expressions)
        .map(this::getTermsForField)
        .filter(termSet -> !termSet.isEmpty())
        .forEach(allTerms::addAll);

    for (Term term : allTerms) {
      String type = term.text();
      boolean grant = type.charAt(type.length() - 1) == 'G';
      Boolean isOwnerExpression = ownerExprMap.get(term.field());

      if (isOwnerExpression == null) {
        if (grant) {
          builder.add(new TermQuery(term), Occur.SHOULD);
        } else {
          builder.add(new TermQuery(term), Occur.MUST_NOT);
        }
      } else {
        if (isOwnerExpression) {
          if (grant) {
            builder.add(new TermQuery(term), Occur.SHOULD);
          } else {
            builder.add(new TermQuery(term), Occur.MUST_NOT);
          }
        }
      }
    }

    return builder.build();
  }
}
