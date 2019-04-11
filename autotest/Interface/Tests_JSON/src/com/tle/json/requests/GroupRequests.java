package com.tle.json.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.SystemTokenProvider;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import java.net.URI;

public class GroupRequests extends AbstractCleanupRequests<String> {
  public GroupRequests(
      URI uri,
      TokenProvider tokens,
      ObjectMapper mapper,
      PageContext pageContext,
      CleanupController cleanupController,
      TestConfig testConfig) {
    super(uri, tokens, mapper, pageContext, cleanupController, testConfig);
  }

  public GroupRequests(
      URI uri,
      ObjectMapper mapper,
      PageContext pageContext,
      CleanupController cleanupController,
      TestConfig testConfig,
      String password) {
    super(
        uri, new SystemTokenProvider(password), mapper, pageContext, cleanupController, testConfig);
  }

  @Override
  public String getId(ObjectNode entity) {
    return entity.get("id").asText();
  }

  @Override
  protected String getBasePath() {
    if (isEquella()) {
      return "api/usermanagement/local/group";
    }
    return "api/localgroup";
  }

  public ObjectNode search(String query) {
    RequestSpecification request = auth().expect().statusCode(200).with();
    request.queryParam("q", query);
    return object(request.get(getResolvedPath() + "/search"));
  }

  public ObjectNode list() {
    RequestSpecification request = successfulRequest();
    return list(request);
  }

  public ObjectNode list(RequestSpecification request) {
    return object(request.get(getResolvedPath() + "/"));
  }

  public ObjectNode users(String groupUuid, boolean recursive) {
    RequestSpecification request = successfulRequest();
    return users(request, groupUuid, recursive);
  }

  public ObjectNode users(RequestSpecification request, String groupUuid, boolean recursive) {
    request = request.param("recursive", recursive);
    if (isEquella()) {
      return object(request.get(getResolvedPath() + "/{uuid}/user", groupUuid));
    }
    return object(request.get(getResolvedPath() + "/{uuid}/users", groupUuid));
  }

  public ObjectNode export() {
    RequestSpecification request = successfulRequest();
    request.queryParam("export", true);
    return object(request.get(getResolvedPath() + "/"));
  }
}
