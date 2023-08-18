package com.tle.core.freetext.index;

import java.io.IOException;
import java.util.function.IntConsumer;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;

public class LuceneDocumentHelper {

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
  public static void forEachDoc(AtomicReader reader, Term term, IntConsumer forEach)
      throws IOException {
    forEachDoc(reader.termDocsEnum(term), forEach);
  }

  /**
   * Count the number of a document ID enumeration and apply the count to the provided function.
   *
   * @param docs Document ID enumeration to be iterated
   * @param useCount Function that consumes the count of documents
   */
  public static void useDocCount(DocIdSetIterator docs, IntConsumer useCount) throws IOException {
    if (docs != null) {
      int count = 0;
      while (docs.nextDoc() != DocsEnum.NO_MORE_DOCS) {
        count++;
      }

      useCount.accept(count);
    }
  }
}
