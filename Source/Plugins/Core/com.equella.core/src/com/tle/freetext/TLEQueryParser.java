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

package com.tle.freetext;

import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.collect.Maps;
import com.tle.common.Check;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.builders.FuzzyQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.PrefixWildcardQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.StandardQueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.standard.builders.WildcardQueryNodeBuilder;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.Operator;
import org.apache.lucene.queryparser.flexible.standard.nodes.PrefixWildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.WildcardQuery;

/**
 * This class works as a Lucene query parser, which is used to parse a query string configured in a
 * search. The main reason to have this class is to support how to parse a query that has some
 * special characters such as "+", "-" and "!".
 *
 * <p>With Lucene V3 and V4, it has been proved that we still need this customised parsing. However,
 * there is a hope that in newer versions, we can drop this class and then simply use
 * `StandardQueryParser`.
 */
public class TLEQueryParser extends StandardQueryParser {

  private static Map<String, String> stemmedToNonStemmed = Maps.newHashMap();
  private static final Pattern pattern =
      Pattern.compile("(?<![\\\\])[-+!]$|(?=[-+!][^\\w\"])(?<![\\\\])[-+!]"); // $NON-NLS-1$

  public TLEQueryParser(
      String[] fields, Analyzer analyzer, Map<String, Float> boosts, Operator defaultOperator) {
    super(analyzer);

    buildStemmedToNonStemmed();

    setMultiFields(fields);
    setFieldsBoost(boosts);
    setAllowLeadingWildcard(true);
    setDefaultOperator(defaultOperator);
    getQueryConfigHandler().unset(ConfigurationKeys.PHRASE_SLOP);

    StandardQueryTreeBuilder queryTreeBuilder = new StandardQueryTreeBuilder();
    queryTreeBuilder.setBuilder(WildcardQueryNode.class, new TLEWildcardQueryNodeBuilder());
    queryTreeBuilder.setBuilder(PrefixWildcardQueryNode.class, new TLEPrefixQueryNodeBuilder());
    queryTreeBuilder.setBuilder(FuzzyQueryNode.class, new TLEFuzzyQueryNodeBuilder());

    setQueryBuilder(queryTreeBuilder);
  }

  public org.apache.lucene.search.Query parse(String rawQuery) throws QueryNodeException {
    /**
     * Seriously ghetto code follows. This is to combat lucene query syntax in item titles. If the
     * title is autocompleted the query should already be escaped (using QueryParser.escape) and if
     * it is manually entered plus "+", hyphen "-" and exclamation "!" should only be considered
     * prohibitors if directly followed by the term. Hopefully this can be removed in future due to
     * the following fix in Lucene 3.6.1 and higher -
     * https://issues.apache.org/jira/browse/LUCENE-2566 Which changes the way - + ! are handled.
     * Does not appear to work correctly for ! though.
     *
     * <p>This method was re-visited when upgrading Lucene to V4.10.4. The above-mentioned issue is
     * still there, even though we replaced the classic QueryParser with the new
     * StandardQueryParser. (Someone also reported this in LUCENE-2566 but nobody respond since
     * then).
     *
     * <p>Plus, in v4 forward slash will cause a QueryNodeParseException if it is not escaped. The
     * Regex pattern defined above is too hard to understand and modifiy without any explanation.
     * Considering our target is V9 which may have solved the issue, maybe in this stage let's just
     * do a simple char replacement to escape forward slash.
     *
     * <p>todo(lucene-upgrade): check whether this custom parsing is needed in future upgrades.
     */
    String query = rawQuery.replace("/", "\\/");
    Matcher matcher = pattern.matcher(query);
    StringBuilder s = new StringBuilder();
    while (matcher.find()) {
      matcher.appendReplacement(s, "\\\\" + matcher.group());
    }
    matcher.appendTail(s);

    String buffered = s.toString();
    if (!Check.isEmpty(buffered)) {
      query = buffered;
    }

    // Use `null` as the default field because the class was originally extended from
    // `MultiFieldQueryParser`
    // where `field` is always `null` when we were using Lucene v3.
    return parse(query, null);
  }

  private void buildStemmedToNonStemmed() {
    stemmedToNonStemmed.put(FreeTextQuery.FIELD_BODY, FreeTextQuery.FIELD_BODY_NOSTEM);
    stemmedToNonStemmed.put(
        FreeTextQuery.FIELD_NAME_VECTORED, FreeTextQuery.FIELD_NAME_VECTORED_NOSTEM);
    stemmedToNonStemmed.put(
        FreeTextQuery.FIELD_ATTACHMENT_VECTORED, FreeTextQuery.FIELD_ATTACHMENT_VECTORED_NOSTEM);
  }

  private static String getNonStemmedField(String stemmedField) {
    if (stemmedField == null
        || stemmedToNonStemmed == null
        || !stemmedToNonStemmed.containsKey(stemmedField)) {
      return stemmedField;
    }

    return stemmedToNonStemmed.get(stemmedField);
  }

  private static FieldQueryNode nonStemmedQueryNode(FieldQueryNode queryNode) {
    String originalField = queryNode.getFieldAsString();
    queryNode.setField(getNonStemmedField(originalField));
    return queryNode;
  }

  private static class TLEWildcardQueryNodeBuilder extends WildcardQueryNodeBuilder {
    @Override
    public WildcardQuery build(QueryNode queryNode) throws QueryNodeException {
      WildcardQueryNode wildcardNode = (WildcardQueryNode) queryNode;
      return super.build(nonStemmedQueryNode(wildcardNode));
    }
  }

  private static class TLEPrefixQueryNodeBuilder extends PrefixWildcardQueryNodeBuilder {
    @Override
    public PrefixQuery build(QueryNode queryNode) throws QueryNodeException {
      PrefixWildcardQueryNode prefixNode = (PrefixWildcardQueryNode) queryNode;
      return super.build(nonStemmedQueryNode(prefixNode));
    }
  }

  private static class TLEFuzzyQueryNodeBuilder extends FuzzyQueryNodeBuilder {
    @Override
    public FuzzyQuery build(QueryNode queryNode) throws QueryNodeException {
      FuzzyQueryNode fuzzyQueryNode = (FuzzyQueryNode) queryNode;
      return super.build(nonStemmedQueryNode(fuzzyQueryNode));
    }
  }
}
