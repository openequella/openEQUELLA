package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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
import org.testng.annotations.Test;

@TestInstitution("rest")
public class AuthApiTest {

  private static final TestConfig TEST_CONFIG = new TestConfig(AuthApiTest.class);
  private static final String USERNAME = "AutoTest";
  private static final String PASSWORD = "automated";
  private static final String API_ENDPOINT = TEST_CONFIG.getInstitutionUrl() + "api/auth";

  private final HttpClient httpClient = new HttpClient();

  @Test
  public void successfulLoginTest() throws IOException {
    final HttpMethod method = buildLoginMethod(USERNAME, PASSWORD);

    final int statusCode = makeClientRequest(method);
    assertEquals(statusCode, HttpStatus.SC_OK, "Attempt to login failed.");
    assertTrue(
        hasAuthenticatedSession(),
        "After a successful login, the session is still a guest session.");
  }

  @Test(dependsOnMethods = "successfulLoginTest")
  public void successfulLogoutTest() throws IOException {
    assertTrue(hasAuthenticatedSession()); // Ensure we're starting the test already logged in
    final String logoutEndpoint = API_ENDPOINT + "/logout";
    final HttpMethod method = new PutMethod(logoutEndpoint);

    final int statusCode = makeClientRequest(method);
    assertEquals(
        statusCode,
        HttpStatus.SC_OK,
        "Attempt to logout failed. (Server said: " + method.getResponseBodyAsString() + ")");
    assertFalse(hasAuthenticatedSession(), "After logout, still has a non-guest session.");
  }

  @Test(dependsOnMethods = "successfulLogoutTest")
  public void invalidCredentialsTest() throws IOException {
    final HttpMethod method = buildLoginMethod("oof", "foo");

    final int statusCode = makeClientRequest(method);
    assertEquals(
        statusCode,
        HttpStatus.SC_UNAUTHORIZED,
        "Incorrect response for (what should be a) failed authentication.");
    assertFalse(
        hasAuthenticatedSession(),
        "A valid session has been established, even though we authenticated with rubbish credentials.");
  }

  private int makeClientRequest(HttpMethod method) throws IOException {
    return httpClient.executeMethod(method);
  }

  private HttpMethod buildLoginMethod(String username, String password) {
    final String loginEndpoint = API_ENDPOINT + "/login";
    final NameValuePair[] queryVals = {
      new NameValuePair("username", username), new NameValuePair("password", password)
    };
    final HttpMethod method = new PostMethod(loginEndpoint);
    method.setQueryString(queryVals);
    return method;
  }

  private boolean hasAuthenticatedSession() throws IOException {
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
