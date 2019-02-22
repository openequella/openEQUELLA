package com.tle.resttests.util;

import static com.tle.resttests.AbstractRestAssuredTest.MAPPER;

import com.tle.json.framework.TokenProvider;
import com.tle.json.requests.AclRequests;
import com.tle.json.requests.CollectionRequests;
import com.tle.json.requests.FacetRequests;
import com.tle.json.requests.InstitutionRequests;
import com.tle.json.requests.ItemRequests;
import com.tle.json.requests.NotificationRequests;
import com.tle.json.requests.OAuthRequests;
import com.tle.json.requests.SchedulerRequests;
import com.tle.json.requests.SchemaRequests;
import com.tle.json.requests.SearchRequests;
import com.tle.json.requests.StagingRequests;
import com.tle.json.requests.TaskRequests;
import com.tle.json.requests.WorkflowRequests;
import com.tle.resttests.AbstractRestAssuredTest;
import java.net.URI;

public class RequestsBuilder {
  private final AbstractRestAssuredTest test;
  private final TokenProvider tokens;
  private final URI baseURI;

  public RequestsBuilder(AbstractRestAssuredTest test, TokenProvider tokens, URI baseURI) {
    this.test = test;
    this.tokens = tokens;
    this.baseURI = baseURI;
  }

  public CollectionRequests collections() {
    return new CollectionRequests(
        baseURI, tokens, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public WorkflowRequests workflows() {
    return new WorkflowRequests(
        baseURI, tokens, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public SchemaRequests schemas() {
    return new SchemaRequests(
        baseURI, tokens, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public StagingRequests staging() {
    return new StagingRequests(baseURI, tokens, MAPPER, test.getContext(), test.getTestConfig());
  }

  public ItemRequests items() {
    return new ItemRequests(baseURI, tokens, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public TaskRequests tasks() {
    return new TaskRequests(baseURI, tokens, MAPPER, test.getContext(), test.getTestConfig());
  }

  public RequestsBuilder user(String userId) {
    return new RequestsBuilder(test, test.getTokens().getProvider(userId), baseURI);
  }

  public RequestsBuilder user(TokenProvider tokenProvider) {
    return new RequestsBuilder(test, tokenProvider, baseURI);
  }

  public SearchRequests searches() {
    return new SearchRequests(baseURI, tokens, MAPPER, test.getContext(), test.getTestConfig());
  }

  public AclRequests acls() {
    return new AclRequests(baseURI, tokens, MAPPER, test.getContext(), test.getTestConfig());
  }

  public FacetRequests facets() {
    return new FacetRequests(baseURI, tokens, MAPPER, test.getContext(), test.getTestConfig());
  }

  public NotificationRequests notifications() {
    return new NotificationRequests(
        baseURI, tokens, MAPPER, test.getContext(), test.getTestConfig());
  }

  public InstitutionRequests institutions(TokenProvider tokenProvider) {
    return new InstitutionRequests(
        baseURI,
        tokenProvider,
        MAPPER,
        test.getContext(),
        test,
        test.getTestConfig(),
        test.getTestConfig().getAdminPassword());
  }

  public InstitutionRequests institutions() {
    return new InstitutionRequests(
        baseURI,
        MAPPER,
        test.getContext(),
        test,
        test.getTestConfig(),
        test.getTestConfig().getAdminPassword());
  }

  public OAuthTokenCache tokenCache() {
    return new OAuthTokenCache(baseURI, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public OAuthRequests oauthClients() {
    return new OAuthRequests(
        baseURI, tokens, MAPPER, test.getContext(), test, test.getTestConfig());
  }

  public SchedulerRequests scheduler() {
    return new SchedulerRequests(baseURI, MAPPER, test.getContext(), test.getTestConfig());
  }

  public AbstractRestAssuredTest getTest() {
    return test;
  }

  public TokenProvider getTokens() {
    return tokens;
  }

  public URI getBaseURI() {
    return baseURI;
  }
}
