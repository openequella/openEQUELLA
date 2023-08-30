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
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.freetext.index.LuceneDocumentHelper;
import java.io.IOException;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.FixedBitSet;

public class InstitutionFilter extends Filter {

  private static final long serialVersionUID = 1L;

  public FixedBitSet getDocIdSet(IndexReaderContext context, Bits acceptDocs) throws IOException {
    FixedBitSet bitSet = new FixedBitSet(context.reader().maxDoc());
    for (LeafReaderContext ctx : context.leaves()) {
      DocIdSetIterator iterator = getDocIdSet(ctx, acceptDocs).iterator();
      LuceneDocumentHelper.forEachDoc(iterator, bitSet::set);
    }

    return bitSet;
  }

  @Override
  public DocIdSet getDocIdSet(LeafReaderContext context, Bits acceptDocs) throws IOException {
    LeafReader reader = context.reader();
    int max = reader.maxDoc();
    FixedBitSet good = new FixedBitSet(max);
    Institution institution = CurrentInstitution.get();
    LuceneDocumentHelper.forEachDoc(
        reader,
        new Term(FreeTextQuery.FIELD_INSTITUTION, Long.toString(institution.getUniqueId())),
        good::set);

    return new BitDocIdSet(good);
  }

  @Override
  public String toString(String field) {
    return null;
  }
}
