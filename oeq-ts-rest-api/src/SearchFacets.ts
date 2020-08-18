/**
 * Params to pass to a call for search facets.
 */
import { is } from 'typescript-is/index';
import { GET } from './AxiosInstance';
import { UuidString } from './Common';

export interface SearchFacetsParams {
  /**
   * List of XML nodes to search.
   */
  nodes: string[];
  /**
   * Currently unused server side. (Kept to ensure a match to the Swagger doco.)
   */
  nestLevel?: string;
  /**
   * A query string for the filter the resultant 'terms' by.
   */
  q?: string;
  /**
   * The number of term combinations to search for, a higher number will return more results and
   * more accurate counts, but will take longer. Default 10.
   */
  breadth?: number;
  /**
   * Collections to filter by.
   */
  collections?: UuidString[];
  /**
   * An Apache Lucene syntax where clause to enable more powerful filtering. More information can
   * be found at the documentation site in the
   * [REST API Guide](https://openequella.github.io/guides/RestAPIGuide.html).
   */
  where?: string;
  /**
   * An ISO date format (yyyy-MM-dd)
   */
  modifiedAfter?: string;
  /**
   * An ISO date format (yyyy-MM-dd)
   */
  modifiedBefore?: string;
  /**
   * An ID (not a username) of a user.
   */
  owner?: UuidString;
  /**
   * If `true` then includes items that are not live.
   */
  showall?: boolean;
}

/**
 * Represents an individual facet returned in the results. Sometimes also called a category.
 *
 * Caution: The is based of the server side class of `com.tle.web.api.search.interfaces.beans.FacetBean`
 * which extends `com.tle.web.api.interfaces.beans.AbstractExtendableBean`. That means at runtime
 * there can be additional fields dynamically added.
 */
export interface Facet {
  /**
   * The derived facet.
   */
  term: string;
  /**
   * How many items match this `term` based on the applied filters (date, owner, collection, etc.)
   */
  count: number;
  /**
   * Additional nested facets - but not currently supported by the server.
   */
  innerFacets?: Facet[];
}

/**
 * Simple container for the results of a facet search. Rather redundant, but had to follow what was
 * `com.tle.web.api.search.interfaces.beans.FacetSearchBean`.
 */
export interface SearchFacetsResult {
  results: Facet[];
}

const SEARCH_FACETS_API_PATH = '/search/facet';

export const searchFacets = (
  apiBasePath: string,
  params?: SearchFacetsParams
): Promise<SearchFacetsResult> =>
  GET<SearchFacetsResult>(
    apiBasePath + SEARCH_FACETS_API_PATH,
    (data): data is SearchFacetsResult => is<SearchFacetsResult>(data),
    params
  );
