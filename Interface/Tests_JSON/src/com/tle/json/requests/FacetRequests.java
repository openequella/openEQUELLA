package com.tle.json.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import java.net.URI;

public class FacetRequests extends AuthorizedRequests {
  public FacetRequests(
      URI baseUri,
      TokenProvider tokenProvider,
      ObjectMapper mapper,
      PageContext pageContext,
      TestConfig testConfig) {
    super(baseUri, tokenProvider, mapper, pageContext, testConfig);
  }

  @Override
  protected String getBasePath() {
    return "api/search/facet";
  }

  public ObjectNode search(RequestSpecification request) {
    return object(request.get(getResolvedPath()));
  }

  public RequestSpecification searchRequest(
      String nodes, String nest, boolean getItems, String query, String filter, String order) {
    RequestSpecification request = successfulRequest();
    request.queryParam("nodes", nodes);
    request.queryParam("getitems", getItems);

    if (nest != null) {
      request.queryParam("nest", nest);
    }
    if (query != null) {
      request.param("q", query);
    }
    if (filter != null) {
      request = request.queryParam("filter", filter);
    }
    if (order != null) {
      request = request.queryParam("order", order);
    }
    return request;
  }

  public ObjectNode search(String nodes, String nest, boolean getItems) {
    return search(searchRequest(nodes, nest, getItems, null, null, null));
  }

  public ObjectNode search(String nodes, String nest, boolean getItems, String query) {
    return search(searchRequest(nodes, nest, getItems, query, null, null));
  }
}
