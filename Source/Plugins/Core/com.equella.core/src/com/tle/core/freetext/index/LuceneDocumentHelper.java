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

import java.io.IOException;
import java.util.function.IntConsumer;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.FixedBitSet;

public final class LuceneDocumentHelper {

  /**
   * * Iterate the provided document ID enumeration and apply each ID to the provided function.
   *
   * @param docs Document ID enumeration to be iterated
   * @param forEach Function that consumes the ID of a document
   */
  public static void forEachDoc(DocIdSetIterator docs, IntConsumer forEach) throws IOException {
    if (docs != null) {
      while (docs.nextDoc() != DocsEnum.NO_MORE_DOCS) {
        forEach.accept(docs.docID());
      }
    }
  }

  /**
   * * Similar to {@link #forEachDoc(DocIdSetIterator, IntConsumer)} but this function uses the
   * provided reader and term to generate the document ID enumeration.
   *
   * @param reader Reader used to get a document enumeration
   * @param term Term for which a document enumeration is generated
   * @param forEach Function that consumes the ID of a document
   */
  public static void forEachDoc(LeafReader reader, Term term, IntConsumer forEach)
      throws IOException {
    forEachDoc(reader.postings(term), forEach);
  }

  /**
   * Count the number of a document ID enumeration and apply the count to the provided function.
   *
   * @param docs Document ID enumeration to be iterated
   * @param useCount Function that consumes the count of documents
   */
  public static void useDocCount(
      DocIdSetIterator docs, FixedBitSet acceptedBits, IntConsumer useCount) throws IOException {
    if (docs != null) {
      int count = 0;
      while (docs.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
        if (acceptedBits != null && acceptedBits.get(docs.docID())) {
          count++;
        }
      }

      useCount.accept(count);
    }
  }
}
