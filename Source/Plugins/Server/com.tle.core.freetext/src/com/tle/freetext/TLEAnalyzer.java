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

package com.tle.freetext;

import java.io.Reader;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * @author aholland
 */
public class TLEAnalyzer extends ReusableAnalyzerBase
{
	private final CharArraySet stopSet;
	private final boolean useStemming;

	public TLEAnalyzer(CharArraySet stopWords, boolean useStemming)
	{
		this.stopSet = stopWords;
		this.useStemming = useStemming;
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader)
	{
		StandardTokenizer tokenizer = new StandardTokenizer(LuceneConstants.LATEST_VERSION, reader);
		TokenStream result = new StandardFilter(LuceneConstants.LATEST_VERSION, tokenizer);
		result = new LowerCaseFilter(LuceneConstants.LATEST_VERSION, result);
		if( stopSet != null )
		{
			result = new StopFilter(LuceneConstants.LATEST_VERSION, result, stopSet);
		}
		if( useStemming )
		{
			result = new PorterStemFilter(result);
		}
		return new TokenStreamComponents(tokenizer, result);
	}
}
