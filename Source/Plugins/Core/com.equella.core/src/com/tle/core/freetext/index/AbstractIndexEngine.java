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

import com.dytech.common.io.FileUtils;
import com.dytech.edge.exceptions.ErrorDuringSearchException;
import com.tle.freetext.TLEAnalyzer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.ControlledRealTimeReopenThread;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When Lucene creates an IndexSearcher it keeps a snap-shot of the index at that point in time, and
 * can be continued to be used even if an IndexModifier is created and makes changes to the index.
 * While the changes will not be reflected in the IndexSearcher until a new one is created, it does
 * allow for EQUELLA to provide well performing search functionality while background indexing
 * operations are underway that do not require their changes to be immediately searchable. "Index
 * Now" changes will destroy the existing IndexSearcher after being written to the index, allowing
 * for the changes to be searchable immediately. The algorithm should look something like this:
 *
 * <ul>
 *   <li><b>If writing/indexing:</b>
 *       <ol>
 *         <li>get write lock
 *         <li>get modifier
 *         <li>write to the index
 *         <li>destroy searcher if item is "high priority"
 *         <li>free the lock
 *       </ol>
 *   <li><b>If searching:</b>
 *       <ol>
 *         <li>if searcher does not exist
 *             <ol>
 *               <li>get read lock
 *               <li>destroy modifier and get new searcher
 *               <li>free the lock
 *             </ol>
 *         <li>perform searches
 *             <ol>
 *         </ul>
 */
@SuppressWarnings("nls")
public abstract class AbstractIndexEngine {
  protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
  private File indexPath;
  private PerFieldAnalyzerWrapper analyzer = null;
  private File stopWordsFile;
  private String analyzerLanguage;
  private FSDirectory directory;

  private IndexWriter indexWriter;
  private SearcherManager searcherManager;
  private ControlledRealTimeReopenThread<IndexSearcher> controlledRealTimeReopenThread;
  private Timer commiterThread;

  // The index generation we should wait for
  private long generation = -1;

  // As autoCompleteAnalyzer doesn't need stopwords and stemming so it works for all languages.
  private final TLEAnalyzer autoCompleteAnalyzer = new TLEAnalyzer(null, false);

  public void deleteDirectory() {
    try {
      commiterThread.cancel();
      controlledRealTimeReopenThread.close();
      searcherManager.close();
      indexWriter.close();
      directory.close();
      FileUtils.delete(indexPath);
      afterPropertiesSet();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @PostConstruct
  public void afterPropertiesSet() throws IOException {
    if (!indexPath.exists()) {
      if (!indexPath.mkdirs()) {
        throw new Error("Error creating index:" + indexPath); // $NON-NLS-1$
      }
    }
    directory = FSDirectory.open(indexPath.toPath());

    LOGGER.info("Opening writer for index:" + indexPath);

    indexWriter = new IndexWriter(directory, new IndexWriterConfig(getAnalyser()));
    searcherManager = new SearcherManager(indexWriter, true, true, null);

    // Possibly reopen a searcher every 5 seconds if necessary in the
    // background
    controlledRealTimeReopenThread =
        new ControlledRealTimeReopenThread<>(indexWriter, searcherManager, 5.0, 0.1);
    controlledRealTimeReopenThread.setName("Controlled Real Time Reopen Thread: " + getClass());
    controlledRealTimeReopenThread.setPriority(
        Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
    controlledRealTimeReopenThread.setDaemon(true);
    controlledRealTimeReopenThread.start();

    // Commit any changes to disk every 5 minutes
    commiterThread = new Timer(true);
    commiterThread.schedule(
        new TimerTask() {
          @Override
          public void run() {
            try {
              indexWriter.commit();
            } catch (IOException ex) {
              LOGGER.error("Error attempting to commit index writer", ex);
            }
          }
        },
        5 * 60 * 1000,
        5 * 60 * 1000);
  }

  public void modifyIndex(IndexBuilder builder) {
    try {
      long g = -1;
      try {
        g = builder.buildIndex(searcherManager, indexWriter);
      } finally {
        generation = Math.max(g, generation);
      }
    } catch (Exception ex) {
      throw new RuntimeException("Error while building index", ex);
    }
  }

  public <RV> RV search(Searcher<RV> s) {
    IndexSearcher indexSearcher = null;
    try {
      controlledRealTimeReopenThread.waitForGeneration(generation);
      indexSearcher = searcherManager.acquire();

      return s.search(indexSearcher);
    } catch (IOException | InterruptedException ex) {
      LOGGER.error("Error searching index", ex);
      throw new ErrorDuringSearchException("Error searching index", ex);
    } finally {
      if (indexSearcher != null) {
        try {
          searcherManager.release(indexSearcher);
        } catch (IOException ex) {
          throw new ErrorDuringSearchException("Error releasing searcher", ex);
        }
      }
    }
  }

  public void setStopWordsFile(File stopWordsFile) {
    this.stopWordsFile = stopWordsFile;
  }

  public void setAnalyzerLanguage(String analyzerLanguage) {
    this.analyzerLanguage = analyzerLanguage;
  }

  /** Returns a new Analyser. */
  protected Analyzer getAnalyser() {
    if (analyzer == null) {
      CharArraySet stopSet = null;
      if (stopWordsFile != null && stopWordsFile.exists()) {
        try {
          stopSet =
              WordlistLoader.getWordSet(new FileReader(stopWordsFile), new CharArraySet(0, true));
        } catch (IOException e1) {
          LOGGER.warn("No stop words available: " + stopWordsFile, e1);
        }
      }
      Analyzer normalAnalyzer = new TLEAnalyzer(stopSet, true);
      TLEAnalyzer nonStemmedAnalyzer = new TLEAnalyzer(stopSet, false);

      // For non-English
      if (!analyzerLanguage.equals("en")) {
        // Load the only one analyzer from a specific language package provided by Lucene
        String languageAnalyzerPackage = "org.apache.lucene.analysis." + analyzerLanguage;
        try (ScanResult scanResult =
            new ClassGraph().enableClassInfo().whitelistPackages(languageAnalyzerPackage).scan()) {
          ClassInfoList languageAnalyzerClasses =
              scanResult.getSubclasses(Analyzer.class.getName());
          List<Class<Analyzer>> languageAnalyzers =
              languageAnalyzerClasses.loadClasses(Analyzer.class);
          Optional<Class<Analyzer>> languageAnalyzer =
              languageAnalyzers.stream()
                  .filter(
                      languageAnalyzerClass ->
                          languageAnalyzerClass.getName().contains(languageAnalyzerPackage))
                  .findFirst();

          if (languageAnalyzer.isPresent()) {
            normalAnalyzer = languageAnalyzer.get().getDeclaredConstructor().newInstance();
            // For the non-stemmed analyzer we still use the TLEAnalyzer, however we use the
            // language specific stop words by loading them from the language specific analyzer.
            Method getDefaultStopSet =
                languageAnalyzer.get().getDeclaredMethod("getDefaultStopSet");
            nonStemmedAnalyzer =
                new TLEAnalyzer(
                    new CharArraySet((Set) getDefaultStopSet.invoke(null), true), false);

            LOGGER.info("Using Lucene analyzer: " + languageAnalyzer.get().getName());
          }
        } catch (InstantiationException
            | NoSuchMethodException
            | InvocationTargetException
            | IllegalAccessException e) {
          // For analyzers that don't have constructors or the getDefaultStopSet method
          normalAnalyzer = autoCompleteAnalyzer;
          nonStemmedAnalyzer = autoCompleteAnalyzer;
          LOGGER.warn(
              analyzerLanguage + " language analyzer is not avaiable so use the default analyzer");
        }
      }
      analyzer =
          new PerFieldAnalyzerWrapper(
              normalAnalyzer, getAnalyzerFieldMap(autoCompleteAnalyzer, nonStemmedAnalyzer));
    }

    return analyzer;
  }

  public TLEAnalyzer getAutoCompleteAnalyzer() {
    return autoCompleteAnalyzer;
  }

  protected abstract Map<String, Analyzer> getAnalyzerFieldMap(
      Analyzer autoComplete, Analyzer nonStemmed);

  public interface Searcher<T> {
    T search(IndexSearcher searcher) throws IOException;
  }

  public interface IndexBuilder {
    /**
     * @return The index generation to wait for, or -1 if you don't care.
     */
    long buildIndex(SearcherManager searcherManager, IndexWriter writer) throws Exception;
  }

  public void setIndexPath(File indexPath) {
    this.indexPath = indexPath;
  }

  public abstract void checkHealth();
}
