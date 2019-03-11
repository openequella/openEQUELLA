package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.annotations.Test;

public class MyResourcesApiTest extends AbstractRestApiTest {
  // @formatter:off
  private final String[][] MAP = {
    {"published", "MyResourcesApiTest - Published"},
    {"draft", "MyResourcesApiTest - Draft"},
    {"modqueue", "MyResourcesApiTest - Moderating"},
    {"archived", "MyResourcesApiTest - Archived"}
  };
  // @formatter:on

  private static final String OAUTH_CLIENT_ID = "MyResourcesApiTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  private JsonNode doMyResourcesSearch(
      String query, String subsearch, Map<?, ?> otherParams, String token) throws Exception {
    List<NameValuePair> params = Lists.newArrayList();
    if (query != null) {
      params.add(new BasicNameValuePair("q", query));
    }
    for (Entry<?, ?> entry : otherParams.entrySet()) {
      params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
    }
    // params.add(new BasicNameValuePair("token", TokenSecurity.createSecureToken(username, "token",
    // "token", null)));
    String paramString = URLEncodedUtils.format(params, "UTF-8");
    HttpGet get =
        new HttpGet(
            context.getBaseUrl() + "api/search/myresources/" + subsearch + "?" + paramString);
    HttpResponse response = execute(get, false, token);
    return mapper.readTree(response.getEntity().getContent());
  }

  @Test
  public void testSubsearch() throws Exception // The rest is covered by SearchApiTest
      {
    String token = requestToken(OAUTH_CLIENT_ID);

    for (String[] arr : MAP) {
      JsonNode resultsNode =
          doMyResourcesSearch(
              "MyResourcesApiTest",
              arr[0],
              ImmutableMap.of("order", "name", "showall", "true"),
              token);
      JsonNode itemsNode = resultsNode.get("results");
      asserter.assertBasic((ObjectNode) itemsNode.get(0), arr[1], null);
    }
    JsonNode resultsNode =
        doMyResourcesSearch(
            "MyResourcesApiTest",
            "all",
            ImmutableMap.of("order", "name", "showall", "true"),
            token);

    assertEquals(resultsNode.get("start").asInt(), 0);
    assertEquals(resultsNode.get("length").asInt(), 4);
    assertEquals(resultsNode.get("available").asInt(), 4);
  }
}
