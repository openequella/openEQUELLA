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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.Maps;
import com.tle.common.Check;

public class TLEQueryParser extends MultiFieldQueryParser
{
	private Map<String, String> stemmedToNonStemmed;
	private static final Pattern pattern = Pattern.compile("(?<![\\\\])[-+!]$|(?=[-+!][^\\w\"])(?<![\\\\])[-+!]"); //$NON-NLS-1$

	public TLEQueryParser(Version version, String[] fields, Analyzer analyzer, Map<String, Float> boosts)
	{
		super(version, fields, analyzer, boosts);
		buildStemmedToNonStemmed();
	}

	@Override
	public org.apache.lucene.search.Query parse(String query) throws ParseException
	{
		/**
		 * Seriously ghetto code follows. This is to combat lucene query syntax
		 * in item titles. If the title is autocompleted the query should
		 * already be escaped (using QueryParser.escape) and if it is manually
		 * entered plus "+", hyphen "-" and exclamation "!" should only be
		 * considered prohibitors if directly followed by the term. Hopefully
		 * this can be removed in future due to the following fix in Lucene
		 * 3.6.1 and higher - https://issues.apache.org/jira/browse/LUCENE-2566
		 * Which changes the way - + ! are handled. Does not appear to work
		 * correctly for ! though.
		 */
		Matcher matcher = pattern.matcher(query);
		StringBuffer s = new StringBuffer();

		while( matcher.find() )
		{
			matcher.appendReplacement(s, "\\\\" + matcher.group()); //$NON-NLS-1$
		}
		matcher.appendTail(s);

		String buffered = s.toString();
		if( !Check.isEmpty(buffered) )
		{
			query = buffered;
		}

		return super.parse(query);
	}

	private void buildStemmedToNonStemmed()
	{
		stemmedToNonStemmed = Maps.newHashMap();
		stemmedToNonStemmed.put(FreeTextQuery.FIELD_BODY, FreeTextQuery.FIELD_BODY_NOSTEM);
		stemmedToNonStemmed.put(FreeTextQuery.FIELD_NAME_VECTORED, FreeTextQuery.FIELD_NAME_VECTORED_NOSTEM);
		stemmedToNonStemmed
			.put(FreeTextQuery.FIELD_ATTACHMENT_VECTORED, FreeTextQuery.FIELD_ATTACHMENT_VECTORED_NOSTEM);
	}

	private String getNonStemmedField(String stemmedField)
	{
		if( stemmedField == null || stemmedToNonStemmed == null || !stemmedToNonStemmed.containsKey(stemmedField) )
		{
			return stemmedField;
		}

		return stemmedToNonStemmed.get(stemmedField);
	}

	@Override
	protected org.apache.lucene.search.Query getWildcardQuery(String field, String termStr) throws ParseException
	{
		return super.getWildcardQuery(getNonStemmedField(field), termStr);
	}

	@Override
	protected org.apache.lucene.search.Query getPrefixQuery(String field, String termStr) throws ParseException
	{
		return super.getPrefixQuery(getNonStemmedField(field), termStr);
	}

	@Override
	protected org.apache.lucene.search.Query getFuzzyQuery(String field, String termStr, float minSimilarity)
		throws ParseException
	{
		return super.getFuzzyQuery(getNonStemmedField(field), termStr, minSimilarity);
	}
}
