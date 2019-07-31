package com.tle.json.requests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import com.tle.json.framework.Waiter;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public abstract class AuthorizedRequests {
  private final URI baseUri;
  private final TokenProvider tokenProvider;
  private final ObjectMapper mapper;
  private final PageContext pageContext;
  private final TestConfig testConfig;

  protected AuthorizedRequests(
      URI baseUri,
      TokenProvider tokenProvider,
      ObjectMapper mapper,
      PageContext pageContext,
      TestConfig testConfig) {
    this.baseUri = baseUri;
    this.tokenProvider = tokenProvider;
    this.mapper = mapper;
    this.pageContext = pageContext;
    this.testConfig = testConfig;
  }

  public RequestSpecification notFoundRequest() {
    return auth().expect().statusCode(404).with();
  }

  public RequestSpecification createRequest() {
    return auth().expect().statusCode(201).with();
  }

  public RequestSpecification accessDeniedRequest() {
    return auth().expect().statusCode(403).with();
  }

  public RequestSpecification badRequest() {
    return auth().expect().statusCode(400).with();
  }

  public RequestSpecification successfulRequest() {
    return auth().expect().statusCode(200).with();
  }

  public RequestSpecification successfulDelete() {
    return auth().expect().statusCode(204).with();
  }

  public RequestSpecification notModified() {
    return auth().expect().statusCode(304).with();
  }

  public RequestSpecification partialContent() {
    return auth().expect().statusCode(206).with();
  }

  protected RequestSpecification auth() {
    String token = tokenProvider.getToken();
    return (token == null
            ? RestAssured.given()
            : RestAssured.given().header("X-Authorization", token))
        .header("X-Autotest-Key", pageContext.getFullName(""));
  }

  protected RequestSpecification jsonBody(RequestSpecification request, ObjectNode body) {
    return request
        .contentType("application/json")
        .body(body.toString().getBytes(Charsets.UTF_8))
        .header("X-Autotest-Key", pageContext.getFullName(""));
  }

  protected JsonNode node(Response response) {
    try {
      return mapper.readTree(response.asByteArray());
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  protected ObjectNode newObject() {
    return mapper.createObjectNode();
  }

  protected ArrayNode newArray() {
    return mapper.createArrayNode();
  }

  protected ObjectNode object(Response response) {
    return (ObjectNode) node(response);
  }

  protected JsonNode list(Response response) {
    return node(response);
  }

  protected String getResolvedPath() {
    return baseUri.resolve(getBasePath()).toString();
  }

  protected abstract String getBasePath();

  public URI getBaseUri() {
    return baseUri;
  }

  public String getLink(ObjectNode object, String type) {
    return object.get("links").get(type).asText();
  }

  public ObjectNode untilSuccess(final Callable<ObjectNode> call) {
    return new Waiter<AuthorizedRequests>(this)
        .withTimeout(20, TimeUnit.SECONDS)
        .until(
            new Function<AuthorizedRequests, ObjectNode>() {
              @Override
              public ObjectNode apply(AuthorizedRequests input) {
                try {
                  return call.call();
                } catch (AssertionError ae) {
                  return null;
                } catch (Exception e) {
                  return null;
                }
              }
            });
  }

  public Waiter<RequestSpecification> requestWaiter(RequestSpecification request) {
    return new Waiter<RequestSpecification>(request).withTimeout(20, TimeUnit.SECONDS);
  }

  protected boolean isEquella() {
    return testConfig.isEquella();
  }
}
