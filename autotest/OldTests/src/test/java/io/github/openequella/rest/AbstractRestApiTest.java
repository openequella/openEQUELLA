package io.github.openequella.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

@TestInstitution("rest")
public class AbstractRestApiTest {

  protected static final String USERNAME = "AutoTest";
  protected static final String PASSWORD = "automated";

  protected TestConfig testConfig = null;

  protected final HttpClient httpClient = new HttpClient();
  protected final ObjectMapper mapper = new ObjectMapper();
  protected final AuthHelper authHelper = new AuthHelper(getTestConfig().getInstitutionUrl());

  public String getAuthApiEndpoint() {
    return getTestConfig().getInstitutionUrl() + "api/auth";
  }

  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(AbstractRestApiTest.class);
    }
    return testConfig;
  }

  @BeforeClass
  public void login() throws IOException {
    makeClientRequest(authHelper.buildLoginMethod(USERNAME, PASSWORD));
  }

  @AfterClass
  public void logout() throws IOException {
    makeClientRequest(authHelper.buildLogoutMethod());
  }

  protected int makeClientRequest(HttpMethod method) throws IOException {
    return httpClient.executeMethod(method);
  }

  protected boolean hasAuthenticatedSession() throws IOException {
    final String userDetailsEndpoint =
        getTestConfig().getInstitutionUrl() + "api/content/currentuser";
    final HttpMethod method = new GetMethod(userDetailsEndpoint);
    if (makeClientRequest(method) != HttpStatus.SC_OK) {
      throw new RuntimeException(
          "Failed to check authentication status, HTTP response code:" + method.getStatusCode());
    }
    String userDetails = method.getResponseBodyAsString();
    return !userDetails.contains("\"id\":\"guest\"");
  }
}
