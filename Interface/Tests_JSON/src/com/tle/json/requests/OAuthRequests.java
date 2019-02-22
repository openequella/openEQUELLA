package com.tle.json.requests;

import static com.jayway.restassured.RestAssured.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tle.json.entity.OAuthClients;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import java.net.URI;

public class OAuthRequests extends BaseEntityRequests {
  public OAuthRequests(
      URI uri,
      TokenProvider token,
      ObjectMapper mapper,
      PageContext context,
      CleanupController cleanupController,
      TestConfig testConfig) {
    super(uri, token, mapper, context, cleanupController, testConfig);
  }

  public String requestToken(String clientId, String password) {
    // @formatter:off
    String token =
        com.jayway.restassured.path.json.JsonPath.from(
                given()
                    .param("grant_type", "client_credentials")
                    .param("client_id", clientId)
                    .param("redirect_uri", "default")
                    .param("client_secret", password)
                    .get(getBaseUri().resolve("oauth/access_token").toString())
                    .asString())
            .get("access_token");
    // @formatter:on
    return "access_token=" + token;
  }

  public String createClient(String clientId, String password, String userId, String redirectUrl) {
    return getId(create(OAuthClients.json(clientId, clientId, password, userId, redirectUrl)));
  }

  @Override
  protected String getBasePath() {
    return "api/oauth";
  }
}
