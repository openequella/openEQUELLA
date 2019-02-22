package com.tle.resttests.test.workflow;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.dytech.devlib.PropBagEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.json.entity.ItemId;
import com.tle.json.framework.PageContext;
import com.tle.json.framework.Waiter;
import com.tle.json.requests.SchedulerRequests;
import com.tle.resttests.AbstractItemApiTest;
import com.tle.resttests.AbstractRestAssuredTest;
import com.tle.resttests.util.RestTestConstants;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Maps;
import org.testng.collections.Sets;

// EQUELLA has no REST endpoint for kicking off a scheduler
@Test(groups = "eps")
public class NotificationsApiTest extends AbstractItemApiTest {
  private static final int TOTAL_NOTIFICATIONS = 2;

  private static final String OAUTH_CLIENT_ID = "NotificationsTestClient";

  private SchedulerRequests scheduler;

  @DataProvider(name = "notificationMap")
  public Object[][] notificationProvider() {
    // @formatter:off
    return new Object[][] {
      //			{ "wentlive", new String[]{"Other User Notifying You"}, new String[]{}},
      //			{ "watchedwentlive", new String[]{"Watched Collection"}, new String[]{"Rejected Item"} },
      {"rejected", new String[] {"Rejected Item"}, new String[] {"Watched Collection"}},
      {"overdue", new String[] {"Moderation Overdue"}, new String[] {"Rejected Item"}}
    };
    // @formatter:on
  }

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, RestTestConstants.USERID_AUTOTEST));
  }

  @Override
  protected void customisePageContext() {
    super.customisePageContext();
    scheduler =
        new SchedulerRequests(
            context.getBaseURI(), AbstractRestAssuredTest.MAPPER, context, testConfig);
  }

  private void waitForIndex(final int totalNotifications, final String type) throws IOException {
    final String token = requestToken(OAUTH_CLIENT_ID);

    Waiter<PageContext> notificationsIndexedWaiter =
        new Waiter<PageContext>(context).withTimeout(50, TimeUnit.SECONDS);
    notificationsIndexedWaiter.until(
        new Predicate<PageContext>() {
          @Override
          public boolean apply(PageContext context) {
            try {
              JsonNode resultsNode =
                  doNotificationsSearch(
                      context.getNamePrefix(),
                      type,
                      ImmutableMap.of("order", "name", "start", -1, "length", 100),
                      token);
              if (resultsNode.get("results").size() != totalNotifications) {
                return false;
              }
            } catch (Exception e) {
              e.printStackTrace();
              Throwables.propagate(e);
            }
            return true;
          }
        });
  }

  @Test(groups = "eps")
  public void setupNotifications() throws Exception {
    // addWatchedClollection();

    // Pretty much ripped straight from NotificationsTest.java
    String REJECTED_ITEM = "Rejected Item";
    String MOD_OVERDUE = "Moderation Overdue";

    // TODO: Not supported by CreateFromScratchTest/eps
    // String OTHER_USER = "Other User Notifying You";

    // String WATCHED_COLLECTION = "Watched Collection";

    ItemId itemId =
        addDeletable(
            createItemWithName(
                COLLECTION_NOTIFICATIONS, MOD_OVERDUE, "item/mod_date", "1980-04-22"));

    // Rejected
    itemId =
        addDeletable(
            createItemWithName(
                COLLECTION_MODERATE, REJECTED_ITEM, "item/controls/checkboxes", "true"));

    taskAction(
        itemId, "ae6e65b2-eeef-4819-b605-ca6b964146c2", "reject", getToken(), "message", "Boo");

    // itemId = addDeletable(createItemWithName(COLLECTION_BASIC,
    // OTHER_USER));

    // itemId = addDeletable(createItemWithName(COLLECTION_BASIC,
    // WATCHED_COLLECTION));

    scheduler.execute("com.tle.core.item.workflow.impl.CheckModerationTask");
    // scheduler.execute("com.tle.core.item.workflow.impl.NotifyOfNewItemsTask");
    scheduler.execute("com.tle.core.item.workflow.impl.CheckReviewTask");

    waitForIndex(TOTAL_NOTIFICATIONS, null);
  }

  // private void addWatchedClollection() throws Exception
  // {
  // String value =
  // "<list><string>b28f1ffe-2008-4f5e-d559-83c8acd79316</string></list>";
  // HttpResponse response = putEntity(value, context.getBaseUrl() +
  // "api/preference/watched.item.definitions",
  // getToken(), true);
  // assertResponse(response, 200, "Setting preferences failed");
  // }

  private ObjectNode createItemWithName(String collection, String name, String... metadataNvs)
      throws IOException {
    ObjectNode itemJson = createItemJson(collection);
    PropBagEx metadata = new PropBagEx();
    metadata.setNode("item/name", context.getNamePrefix(name));
    for (int i = 0; i < metadataNvs.length; i++) {
      String path = metadataNvs[i];
      String value = metadataNvs[++i];
      metadata.setNode(path, value);
    }
    itemJson.put("metadata", metadata.toString());
    return createItem(itemJson.toString(), getToken(), "waitforindex", true);
  }

  @Test(dependsOnMethods = "setupNotifications", dataProvider = "notificationMap", groups = "eps")
  public void getAndClearNotifications(
      String reason, String[] expectedResults, String[] notExpected) throws Exception {
    final String token = requestToken(OAUTH_CLIENT_ID);

    JsonNode resultsNode =
        doNotificationsSearch(
            context.getNamePrefix(), reason, ImmutableMap.of("order", "name"), token);

    JsonNode notificationsNode = resultsNode.get("results");
    Set<String> expectedNames = Sets.newHashSet();
    for (String expected : expectedResults) {
      expectedNames.add(context.getNamePrefix(expected));
    }
    Set<String> unExpectedNames = Sets.newHashSet();
    for (String expected : notExpected) {
      unExpectedNames.add(context.getNamePrefix(expected));
    }
    for (JsonNode notification : notificationsNode) {
      String itemUri = notification.get("item").get("links").get("self").asText();
      String name = getItemName(itemUri);
      if (!expectedNames.remove(name)) {
        assertFalse(unExpectedNames.contains(name), "Did not expect to see :" + name);
      } else {
        assertResponse(
            deleteResource(notification.get("links").get("delete").asText(), getToken()),
            200,
            "Expected to be able to delete");
      }
    }
    assertEquals(expectedNames.size(), 0, "Expected to see: " + expectedNames);
  }

  @Test(dependsOnMethods = "getAndClearNotifications", groups = "eps")
  public void checkDeletions() throws Exception {
    waitForIndex(0, "nonExistantCountsAsAll?");

    JsonNode results =
        doNotificationsSearch("dontfindanything", null, Maps.newHashMap(), getToken());
    assertEquals(results.get("available").asInt(), 0, "Should be no notifications");
  }

  private JsonNode doNotificationsSearch(
      String query, String subsearch, Map<?, ?> otherParams, String token) throws Exception {
    List<NameValuePair> params = Lists.newArrayList();
    if (query != null) {
      params.add(new BasicNameValuePair("q", query));
    }
    if (subsearch != null) {
      params.add(new BasicNameValuePair("type", subsearch));
    }
    for (Entry<?, ?> entry : otherParams.entrySet()) {
      params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
    }
    String paramString = URLEncodedUtils.format(params, "UTF-8");
    HttpGet get = new HttpGet(context.getBaseUrl() + "api/notification?" + paramString);
    HttpResponse response = execute(get, false, token);
    return mapper.readTree(response.getEntity().getContent());
  }

  private String getItemName(String itemUri) throws Exception {
    ObjectNode item = getItem(itemUri, "basic", getToken());
    return item.get("name").textValue();
  }
}
