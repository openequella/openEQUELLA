package io.github.openequella.rest;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

@TestInstitution("rest")
public class AbstractRestApiTest {

  protected static final TestConfig TEST_CONFIG = new TestConfig(AbstractRestApiTest.class);
  protected static final String USERNAME = "AutoTest";
  protected static final String PASSWORD = "automated";
  private static final String AUTH_API_ENDPOINT = TEST_CONFIG.getInstitutionUrl() + "api/auth";

  protected final HttpClient httpClient = new HttpClient();
  protected final ObjectMapper mapper = new ObjectMapper();

  @BeforeClass
  public void login() throws IOException {
    makeClientRequest(buildLoginMethod(USERNAME, PASSWORD));
  }

  @AfterClass
  public void logout() throws IOException {
    makeClientRequest(buildLogoutMethod());
  }

  protected int makeClientRequest(HttpMethod method) throws IOException {
    return httpClient.executeMethod(method);
  }

  protected HttpMethod buildLoginMethod(String username, String password) {

    final String loginEndpoint = AUTH_API_ENDPOINT + "/login";
    final NameValuePair[] queryVals = {
      new NameValuePair("username", username), new NameValuePair("password", password)
    };
    final HttpMethod method = new PostMethod(loginEndpoint);
    method.setQueryString(queryVals);
    return method;
  }

  protected HttpMethod buildLogoutMethod() {
    final String logoutEndpoint = AUTH_API_ENDPOINT + "/logout";
    final HttpMethod method = new PutMethod(logoutEndpoint);
    return method;
  }

  protected boolean hasAuthenticatedSession() throws IOException {
    final String userDetailsEndpoint = TEST_CONFIG.getInstitutionUrl() + "api/content/currentuser";
    final HttpMethod method = new GetMethod(userDetailsEndpoint);
    if (makeClientRequest(method) != HttpStatus.SC_OK) {
      throw new RuntimeException(
          "Failed to check authentication status, HTTP response code:" + method.getStatusCode());
    }
    String userDetails = method.getResponseBodyAsString();
    return !userDetails.contains("\"id\":\"guest\"");
  }
}
