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

import java.io.IOException;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.OpenBitSet;

/**
 * Filters string by comparison. This is inclusive.
 *
 * @author Nicholas Read
 */
public class ComparisonFilter extends Filter {

  private static final long serialVersionUID = 1L;
  private final String field;
  private final String start;
  private final String end;

  public ComparisonFilter(String field, String start, String end) {
    this.field = field;
    this.start = start;
    this.end = end;
  }

  @Override
  public DocIdSet getDocIdSet(AtomicReaderContext context, Bits acceptDocs) throws IOException {
    AtomicReader reader = context.reader();
    OpenBitSet bits = new OpenBitSet(reader.maxDoc());

    Terms terms = reader.terms(field);
    if (terms != null) {
      TermsEnum termsEnum = terms.iterator(null);
      BytesRef startTerm = new BytesRef(start);
      BytesRef endTerm = new BytesRef(end);

      termsEnum.seekCeil(startTerm);

      for (startTerm = termsEnum.term();
          startTerm != null && startTerm.compareTo(endTerm) <= 0;
          startTerm = termsEnum.next()) {
        DocsEnum termDocs = reader.termDocsEnum(new Term(field, startTerm));
        while (termDocs != null && termDocs.nextDoc() != DocsEnum.NO_MORE_DOCS) {
          bits.set(termDocs.docID());
        }
      }
    }

    return bits;
  }
}
