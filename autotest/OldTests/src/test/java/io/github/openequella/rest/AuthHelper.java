package io.github.openequella.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.UnsupportedEncodingException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

/**
 * Helper class to assist in interacting the with {@code api/auth} endpoint, primarily through the
 * building of {@code HttpMethod} instances.
 *
 * <p>This class is designed for reuse across multiple test client implementations that require
 * session-based authentication against the REST API.
 */
public class AuthHelper {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private String institutionUrl;

  public AuthHelper(String institutionUrl) {
    this.institutionUrl = institutionUrl;
  }

  public String getAuthApiEndpoint() {
    return institutionUrl + "api/auth";
  }

  public HttpMethod buildLogoutMethod() {
    final String logoutEndpoint = getAuthApiEndpoint() + "/logout";
    return new PutMethod(logoutEndpoint);
  }

  public HttpMethod buildLoginMethod(String username, String password)
      throws UnsupportedEncodingException {
    final String loginEndpoint = getAuthApiEndpoint() + "/login";
    // Credentials are sent as a JSON body as required by api/auth/login.
    final ObjectNode credentials = MAPPER.createObjectNode();
    credentials.put("username", username);
    credentials.put("password", password);
    final PostMethod method = new PostMethod(loginEndpoint);
    method.setRequestEntity(
        new StringRequestEntity(credentials.toString(), "application/json", "UTF-8"));
    return method;
  }
}
