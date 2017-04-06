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
