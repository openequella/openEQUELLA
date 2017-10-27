/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NRTManager;
import org.apache.lucene.search.NRTManagerReopenThread;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.FSDirectory;

import com.dytech.common.io.FileUtils;
import com.dytech.edge.exceptions.ErrorDuringSearchException;
import com.google.common.base.Throwables;
import com.tle.freetext.LuceneConstants;
import com.tle.freetext.TLEAnalyzer;

/**
 * When Lucene creates an IndexSearcher it keeps a snap-shot of the index at
 * that point in time, and can be continued to be used even if an IndexModifier
 * is created and makes changes to the index. While the changes will not be
 * reflected in the IndexSearcher until a new one is created, it does allow for
 * EQUELLA to provide well performing search functionality while background
 * indexing operations are underway that do not require their changes to be
 * immediately searchable. "Index Now" changes will destroy the existing
 * IndexSearcher after being written to the index, allowing for the changes to
 * be searchable immediately. The algorithm should look something like this:
 * <ul>
 * <li><b>If writing/indexing:</b></li>
 * <ol>
 * <li>get write lock</li>
 * <li>get modifier</li>
 * <li>write to the index</li>
 * <li>destroy searcher if item is "high priority"</li>
 * <li>free the lock</li>
 * </ol>
 * <li><b>If searching:</b></li>
 * <ol>
 * <li>if searcher does not exist</li>
 * <ol>
 * <li>get read lock</li>
 * <li>destroy modifier and get new searcher</li>
 * <li>free the lock</li>
 * </ol>
 * <li>perform searches</li>
 * <ol>
 * </ul>
 */
@SuppressWarnings("nls")
public abstract class AbstractIndexEngine
{
	protected final Logger LOGGER = Logger.getLogger(getClass());

	private File indexPath;
	private PerFieldAnalyzerWrapper analyzer = null;
	private File stopWordsFile;
	private FSDirectory directory;
	private IndexWriter indexWriter;
	private NRTManager nrtManager;
	private NRTManagerReopenThread nrtReopenThread;
	private Timer commiterThread;

	// The index generation we should wait for
	private long generation = -1;

	public void deleteDirectory()
	{
		try
		{
			commiterThread.cancel();
			nrtReopenThread.close();
			nrtManager.close();
			indexWriter.close();
			directory.close();
			FileUtils.delete(indexPath);
			afterPropertiesSet();
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@PostConstruct
	public void afterPropertiesSet() throws IOException
	{
		if( !indexPath.exists() )
		{
			if( !indexPath.mkdirs() )
			{
				throw new Error("Error creating index:" + indexPath); //$NON-NLS-1$
			}
		}
		directory = FSDirectory.open(indexPath);

		if( IndexWriter.isLocked(directory) )
		{
			LOGGER.info("Unlocking index:" + indexPath); //$NON-NLS-1$
			IndexWriter.unlock(directory);
		}
		LOGGER.info("Opening writer for index:" + indexPath); //$NON-NLS-1$
		indexWriter = new IndexWriter(directory, new IndexWriterConfig(LuceneConstants.LATEST_VERSION, getAnalyser()));
		nrtManager = new NRTManager(indexWriter, null);

		// Possibly reopen a searcher every 5 seconds if necessary in the
		// background
		nrtReopenThread = new NRTManagerReopenThread(nrtManager, 5.0, 0.1);
		nrtReopenThread.setName("NRT Reopen Thread: " + getClass());
		nrtReopenThread.setPriority(Math.min(Thread.currentThread().getPriority() + 2, Thread.MAX_PRIORITY));
		nrtReopenThread.setDaemon(true);
		nrtReopenThread.start();

		// Commit any changes to disk every 5 minutes
		commiterThread = new Timer(true);
		commiterThread.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				try
				{
					indexWriter.commit();
				}
				catch( IOException ex )
				{
					LOGGER.error("Error attempting to commit index writer", ex);
				}
			}
		}, 5 * 60 * 1000, 5 * 60 * 1000);
	}

	public void modifyIndex(IndexBuilder builder)
	{
		try
		{
			long g = -1;
			try
			{
				g = builder.buildIndex(nrtManager);
			}
			finally
			{
				generation = Math.max(g, generation);
			}
		}
		catch( Exception ex )
		{
			throw new RuntimeException("Error while building index", ex); //$NON-NLS-1$
		}
	}

	public <RV> RV search(Searcher<RV> s)
	{
		SearcherManager manager = nrtManager.waitForGeneration(generation, true);
		IndexSearcher indexSearcher = null;
		try
		{
			indexSearcher = manager.acquire();
			return s.search(indexSearcher);
		}
		catch( IOException ex )
		{
			LOGGER.error("Error searching index", ex); //$NON-NLS-1$
			throw new ErrorDuringSearchException("Error searching index", ex); //$NON-NLS-1$
		}
		finally
		{
			if( indexSearcher != null )
			{
				try
				{
					manager.release(indexSearcher);
				}
				catch( IOException ex )
				{
					throw new ErrorDuringSearchException("Error releasing searcher", ex); //$NON-NLS-1$
				}
			}
		}
	}

	public void setStopWordsFile(File stopWordsFile)
	{
		this.stopWordsFile = stopWordsFile;
	}

	/**
	 * Returns a new Analyser.
	 */
	protected Analyzer getAnalyser()
	{
		if( analyzer == null )
		{
			CharArraySet stopSet = null;
			if( stopWordsFile != null && stopWordsFile.exists() )
			{
				try
				{
					stopSet = WordlistLoader.getWordSet(new FileReader(stopWordsFile), new CharArraySet(
						LuceneConstants.LATEST_VERSION, 0, true));
				}
				catch( IOException e1 )
				{
					LOGGER.warn("No stop words available: " + stopWordsFile, e1);
				}
			}
			TLEAnalyzer normalAnalyzer = new TLEAnalyzer(stopSet, true);
			TLEAnalyzer autoCompleteAnalyzer = new TLEAnalyzer(null, false);
			TLEAnalyzer nonStemmedAnalyzer = new TLEAnalyzer(stopSet, false);
			analyzer = new PerFieldAnalyzerWrapper(normalAnalyzer, getAnalyzerFieldMap(autoCompleteAnalyzer,
				nonStemmedAnalyzer));
		}

		return analyzer;
	}

	protected abstract Map<String, Analyzer> getAnalyzerFieldMap(Analyzer autoComplete, Analyzer nonStemmed);

	public interface Searcher<T>
	{
		T search(IndexSearcher searcher) throws IOException;
	}

	public interface IndexBuilder
	{
		/**
		 * @return The index generation to wait for, or -1 if you don't care.
		 */
		long buildIndex(NRTManager nrtManager) throws Exception;
	}

	public void setIndexPath(File indexPath)
	{
		this.indexPath = indexPath;
	}

	public abstract void checkHealth();
}
