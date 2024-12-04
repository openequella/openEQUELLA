package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.testng.annotations.Test;

@TestInstitution("rest")
public class AuthApiTest extends AbstractRestApiTest {

  @Test
  public void successfulLoginTest() throws IOException {
    final HttpMethod method = authHelper.buildLoginMethod(USERNAME, PASSWORD);

    final int statusCode = makeClientRequest(method);
    assertEquals(statusCode, HttpStatus.SC_OK, "Attempt to login failed.");
    assertTrue(
        hasAuthenticatedSession(),
        "After a successful login, the session is still a guest session.");
  }

  @Test(dependsOnMethods = "successfulLoginTest")
  public void successfulLogoutTest() throws IOException {
    assertTrue(hasAuthenticatedSession()); // Ensure we're starting the test already logged in
    final HttpMethod method = authHelper.buildLogoutMethod();

    final int statusCode = makeClientRequest(method);
    assertEquals(
        statusCode,
        HttpStatus.SC_OK,
        "Attempt to logout failed. (Server said: " + method.getResponseBodyAsString() + ")");
    assertFalse(hasAuthenticatedSession(), "After logout, still has a non-guest session.");
  }

  @Test(dependsOnMethods = "successfulLogoutTest")
  public void invalidCredentialsTest() throws IOException {
    final HttpMethod method = authHelper.buildLoginMethod("oof", "foo");

    final int statusCode = makeClientRequest(method);
    assertEquals(
        statusCode,
        HttpStatus.SC_UNAUTHORIZED,
        "Incorrect response for (what should be a) failed authentication.");
    assertFalse(
        hasAuthenticatedSession(),
        "A valid session has been established, even though we authenticated with rubbish"
            + " credentials.");
  }
}
