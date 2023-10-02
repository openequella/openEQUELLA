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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiTerms;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;

/**
 * This filter was significantly refactored when we upgraded Lucene V5.5.5. To generate a proper
 * query for this filter, a big change about how to handle the Owner ACL was required.
 *
 * <p>An Owner ACL refers to an ACL that is granted or revoked for `OWNER` or for a complex
 * expression where `OWNER` is part of the expression. If a user account has Owner related ACLs, we
 * must create a BooleanQuery which contains not only the Owner ACL term but also the OWNER term of
 * the current user.
 *
 * <p>If the user account does not have any Owner related ACLs, then we do not need such a
 * BooleanQuery.
 * <li>Example 1: an Owner ACL (ACLD-1111:000G) and a common ACL (ACLD-2222:000G) are available in
 *     user account A, the result is ((+owner:A +ACLD-1111:000G) ACLD-2222:000G).
 * <li>Example 2: an Owner ACL (ACLD-1111:000R) and a common ACL (ACLD-2222:000G) are available in
 *     user account A, the result is ((-owner:A +ACLD-1111:000G) ACLD-2222:000G).
 *
 *     <p>Please note that the value of Owner ACL is <b>000R</b>, so the prefix before owner in the
 *     query is <b>-</b>.
 *
 *     <p>The translation of this expression is: you can access resources that are available to
 *     OWNER, but the owner must not be A.
 * <li>Example 3: two common ACLs (ACLD-2222 and ACLD-3333) are available in user account A, the
 *     result is (ACLD-2222 OR ACLD-3333).
 */
public class SecurityFilter implements CustomFilter {
  private final String[] expressions;
  private final Map<String, Boolean> ownerExprMap = new HashMap<>();
  private final boolean systemUser;
  private final IndexReader reader;

  public SecurityFilter(String aclType, IndexReader reader) {
    this.reader = reader;

    UserState userState = CurrentUser.getUserState();
    systemUser = userState.isSystem();
    Collection<Long> aclExpressions = userState.getCommonAclExpressions();
    Collection<Long> ownerAclExpressions = userState.getOwnerAclExpressions();
    Collection<Long> notOwnerAclExpressions = userState.getNotOwnerAclExpressions();

    int ownerSizes =
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
      Terms terms = MultiTerms.getTerms(reader, field);
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
    if (systemUser) {
      return null;
    }

    Set<Term> allTerms = new TreeSet<>();

    Arrays.stream(expressions)
        .map(this::getTermsForField)
        .filter(termSet -> !termSet.isEmpty())
        .forEach(allTerms::addAll);

    Builder fullQueryBuilder = new Builder();
    for (Term term : allTerms) {
      Optional<Boolean> ownerAcl = Optional.ofNullable(ownerExprMap.get(term.field()));
      if (ownerAcl.isPresent()) {
        boolean isOwnerAcl = ownerAcl.get();
        Builder ownerClauseBuilder = new Builder();
        ownerClauseBuilder.add(new TermQuery(term), Occur.MUST);
        ownerClauseBuilder.add(
            new TermQuery(
                new Term(FreeTextQuery.FIELD_OWNER, new BytesRef(CurrentUser.getUserID()))),
            isOwnerAcl ? Occur.MUST : Occur.MUST_NOT);
        fullQueryBuilder.add(ownerClauseBuilder.build(), Occur.SHOULD);
      } else {
        String type = term.text();
        boolean grant = type.endsWith("G");
        if (grant) {
          fullQueryBuilder.add(new TermQuery(term), Occur.SHOULD);
        } else {
          fullQueryBuilder.add(new TermQuery(term), Occur.MUST_NOT);
        }
      }
    }

    return fullQueryBuilder.build();
  }
}
