package com.tle.resttests.util;

import static com.tle.resttests.AbstractRestAssuredTest.MAPPER;

import com.tle.json.framework.TokenProvider;
import com.tle.json.requests.GroupRequests;
import com.tle.json.requests.RoleRequests;
import com.tle.json.requests.UserRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import java.net.URI;

public class UserRequestsBuilder {
  private final AbstractRestAssuredTest test;
  // private final TokenProvider tokens;
  private final URI baseURI;

  public UserRequestsBuilder(RequestsBuilder builder) {
    this(builder.getTest(), builder.getTokens(), builder.getBaseURI());
  }

  public UserRequestsBuilder(AbstractRestAssuredTest test) {
    this(test, test, test.getContext().getBaseURI());
  }

  public UserRequestsBuilder(AbstractRestAssuredTest test, TokenProvider tokens, URI baseURI) {
    this.test = test;
    // this.tokens = tokens;
    this.baseURI = baseURI;
  }

  public UserRequests users() {
    return new UserRequests(
        baseURI, MAPPER, test.getContext(), test, test.getTestConfig(), "tle010");
  }

  public UserRequests users(TokenProvider tokenProvider) {
    return new UserRequests(
        baseURI, tokenProvider, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public GroupRequests groups() {
    return new GroupRequests(
        baseURI, MAPPER, test.getContext(), test, test.getTestConfig(), "tle010");
  }

  public GroupRequests groups(TokenProvider tokenProvider) {
    return new GroupRequests(
        baseURI, tokenProvider, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public RoleRequests roles() {
    return new RoleRequests(
        baseURI, MAPPER, test.getContext(), test, test.getTestConfig(), "tle010");
  }

  public RoleRequests roles(TokenProvider tokenProvider) {
    return new RoleRequests(
        baseURI, tokenProvider, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public RequestsBuilder user(String userId) {
    return new RequestsBuilder(test, test.getTokens().getProvider(userId), baseURI);
  }
}
