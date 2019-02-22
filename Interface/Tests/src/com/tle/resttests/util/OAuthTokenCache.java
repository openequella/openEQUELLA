package com.tle.resttests.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.json.framework.CleanupController;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.TestConfig;
import com.tle.json.framework.TokenProvider;
import com.tle.json.requests.OAuthRequests;
import java.net.URI;
import org.testng.internal.collections.Pair;

public class OAuthTokenCache {
  private String prefix;
  private String adminPassword;
  private TokenProvider adminToken;

  private OAuthRequests requests;

  private LoadingCache<String, Pair<String, String>> clientCache =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<String, Pair<String, String>>() {
                @Override
                public Pair<String, String> load(String userId) throws Exception {
                  String clientId = prefix + userId;
                  String uuid = null;
                  JsonNode results = requests.list().get("results");
                  for (JsonNode jsonNode : results) {
                    JsonNode clientIdNode = jsonNode.get("clientId");
                    if (clientId.equals(clientIdNode.asText())) {
                      uuid = jsonNode.get("uuid").asText();
                      break;
                    }
                  }
                  if (uuid == null) {
                    uuid = requests.createClient(clientId, adminPassword, userId, "default");
                  }
                  return new Pair<String, String>(clientId, uuid);
                }
              });

  private LoadingCache<String, String> tokenMap =
      CacheBuilder.newBuilder()
          .build(
              new CacheLoader<String, String>() {
                @Override
                public String load(String userId) throws Exception {
                  String clientId = clientCache.get(userId).first();
                  return requests.requestToken(clientId, adminPassword);
                }
              });

  public OAuthTokenCache(
      URI baseUri,
      ObjectMapper mapper,
      PageContext context,
      CleanupController cleanupController,
      TestConfig testConfig) {
    adminPassword = testConfig.getAdminPassword();
    adminToken =
        new TokenProvider() {
          @Override
          public String getToken() {
            return "admin_token=" + adminPassword;
          }
        };
    requests =
        new OAuthRequests(baseUri, adminToken, mapper, context, cleanupController, testConfig);
    this.prefix = context.getNamePrefix();
  }

  public String getToken(String userId) {
    return tokenMap.getUnchecked(userId);
  }

  public void invalidate(String userId) {
    Pair<String, String> clientInfo = clientCache.getIfPresent(userId);
    if (clientInfo != null) {
      requests.delete(clientInfo.second());
      clientCache.invalidate(userId);
      tokenMap.invalidate(userId);
    }
  }

  public TokenProvider getProvider(final String userId) {
    return new TokenProvider() {
      @Override
      public String getToken() {
        return OAuthTokenCache.this.getToken(userId);
      }
    };
  }
}
