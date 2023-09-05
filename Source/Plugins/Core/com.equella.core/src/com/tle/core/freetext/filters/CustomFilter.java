package com.tle.core.freetext.filters;

import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;

/**
 * Interface for custom search filters used in Lucene searches. Since Lucene V5, directly using
 * filters is deprecated and the correct way is to build a query that represents the filter and
 * specify how the query matches documents.
 */
public interface CustomFilter {

  /**
   * Specifies how a filter matches documents. The default option is `FILTER` which means clauses of
   * the filter MUST appear in the matching documents but do not participate in scoring.
   */
  default Occur getOccur() {
    return Occur.FILTER;
  }

  /**
   * Build a query to represent the filter. If this method returns `null`, it means the filter is
   * not needed ,or it does not have an equivalent query.
   *
   * <p>For example, TLE ADMINISTRATOR does not need Security Filter, and a list of empty Must
   * Clauses does not really have a query representation.
   */
  Query buildQuery();
}
