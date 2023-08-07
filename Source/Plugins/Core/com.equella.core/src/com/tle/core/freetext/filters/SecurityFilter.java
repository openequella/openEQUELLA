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
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.OpenBitSet;

public class SecurityFilter extends Filter {
  private static final long serialVersionUID = 1L;

  private OpenBitSet results;
  private boolean onlyCollectResults;

  private String[] expressions;
  private Map<String, Boolean> ownerExprMap;
  private TermValueComparator comparator = new TermValueComparator();
  private int ownerSizes;
  private boolean systemUser;

  public SecurityFilter(String aclType) {
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

  public OpenBitSet getResults() {
    return results;
  }

  public void setOnlyCollectResults(boolean onlyCollectResults) {
    this.onlyCollectResults = onlyCollectResults;
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
    AtomicReader reader = context.reader();
    final int max = reader.maxDoc();
    results = new OpenBitSet(max);

    if (!systemUser) {
      OpenBitSet owned = new OpenBitSet(max);
      if (ownerSizes > 0) {
        DocsEnum odocs =
            reader.termDocsEnum(new Term(FreeTextQuery.FIELD_OWNER, CurrentUser.getUserID()));
        while (odocs != null && odocs.nextDoc() != DocsEnum.NO_MORE_DOCS) {
          owned.set(odocs.docID());
        }
      }

      Set<Term> allTerms = new TreeSet<Term>(comparator);
      for (String field : expressions) {
        Terms terms = MultiFields.getTerms(reader, field);
        if (terms != null) {
          TermsEnum termsEnum = terms.iterator(null);
          termsEnum.seekCeil(new BytesRef(""));
          while (termsEnum.next() != null) {
            String text = termsEnum.term().utf8ToString();
            allTerms.add(new Term(field, new BytesRef(text)));
          }
          ;
        }
      }

      for (Term term : allTerms) {
        String type = term.text();
        boolean grant = type.charAt(type.length() - 1) == 'G';
        DocsEnum docs = reader.termDocsEnum(term);
        Boolean exprType = ownerExprMap.get(term.field());

        int doc;
        if (exprType == null) {
          if (grant) {
            while ((doc = docs.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
              results.set(doc);
            }
          } else {
            while ((doc = docs.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
              results.clear(doc);
            }
          }
        } else {
          boolean must = exprType;
          while ((doc = docs.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
            if (owned.get(doc) == must) {
              if (grant) {
                results.set(doc);
              } else {
                results.clear(doc);
              }
            }
          }
        }
      }
    } else {
      Bits liveDocs = reader.getLiveDocs();
      if (liveDocs != null) {
        for (int i = 0; i < liveDocs.length(); i++) {
          results.set(i);
        }
      } else {
        for (int i = 0; i < reader.maxDoc(); i++) {
          results.set(i);
        }
      }
    }

    // If we are only collecting results, we return a full bitset to match
    // every document.
    if (onlyCollectResults) {
      OpenBitSet fullBitSet = new OpenBitSet(max);
      fullBitSet.set(0, max);
      return fullBitSet;
    } else {
      return results;
    }
  }

  public static class TermValueComparator implements Comparator<Term>, Serializable {
    @Override
    public int compare(Term o1, Term o2) {
      int comp = o2.text().compareTo(o1.text());
      if (comp == 0) {
        return o1.field().compareTo(o2.field());
      }
      return comp;
    }
  }
}
