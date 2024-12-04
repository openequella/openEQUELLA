package io.github.openequella.rest;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;

/**
 * Helper class to assist in interacting the with {@code api/auth} endpoint, primarily through the
 * building of {@code HttpMethod} instances.
 */
public class AuthHelper {
  private String institutionUrl;

  public AuthHelper(String institutionUrl) {
    this.institutionUrl = institutionUrl;
  }
  ;

  public String getAuthApiEndpoint() {
    return institutionUrl + "api/auth";
  }

  protected HttpMethod buildLogoutMethod() {
    final String logoutEndpoint = getAuthApiEndpoint() + "/logout";
    return new PutMethod(logoutEndpoint);
  }

  protected HttpMethod buildLoginMethod(String username, String password) {
    final String loginEndpoint = getAuthApiEndpoint() + "/login";
    final NameValuePair[] queryVals = {
      new NameValuePair("username", username), new NameValuePair("password", password)
    };
    final HttpMethod method = new PostMethod(loginEndpoint);
    method.setQueryString(queryVals);
    return method;
  }
}
