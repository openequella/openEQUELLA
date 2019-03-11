package com.tle.json.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import com.tle.json.framework.Waiter;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class NotificationRequests extends AuthorizedRequests {
  public NotificationRequests(
      URI baseUri,
      TokenProvider tokenProvider,
      ObjectMapper mapper,
      PageContext pageContext,
      TestConfig testConfig) {
    super(baseUri, tokenProvider, mapper, pageContext, testConfig);
  }

  @Override
  protected String getBasePath() {
    return "api/notification";
  }

  public ObjectNode search(RequestSpecification request) {
    return object(request.get(getResolvedPath()));
  }

  public RequestSpecification searchRequest(String query, String type) {
    RequestSpecification request = successfulRequest().param("q", query);
    if (type != null) {
      request = request.queryParam("type", type);
    }
    return request;
  }

  public ObjectNode search(String query) {
    return search(searchRequest(query, null));
  }

  public ObjectNode waitForNotification(
      final ItemId expectedId, final String query, final String reason) {
    return waitUntil(searchRequest(query, null), resultAvailable(expectedId, reason));
  }

  public <T> T waitUntil(RequestSpecification request, final Function<ObjectNode, T> until) {
    Waiter<RequestSpecification> indexWaiter =
        new Waiter<RequestSpecification>(request).withTimeout(60, TimeUnit.SECONDS);
    return indexWaiter.until(
        new Function<RequestSpecification, T>() {
          @Override
          public T apply(RequestSpecification request) {
            ObjectNode taskResults = search(request);
            return until.apply(taskResults);
          }
        });
  }

  public static Function<ObjectNode, ObjectNode> resultAvailable(
      final ItemId expectedId, final String reason) {
    return new Function<ObjectNode, ObjectNode>() {
      @Override
      public ObjectNode apply(ObjectNode taskResults) {
        JsonNode results = taskResults.get("results");
        for (JsonNode result : results) {
          JsonNode itemNode = result.get("item");
          ItemId itemId =
              new ItemId(itemNode.get("uuid").asText(), itemNode.get("version").asInt());
          if (itemId.equals(expectedId)) {
            if (reason != null && !result.get("reason").asText().equals(reason)) {
              return null;
            }
            return (ObjectNode) result;
          }
        }
        return null;
      }
    };
  }
}
