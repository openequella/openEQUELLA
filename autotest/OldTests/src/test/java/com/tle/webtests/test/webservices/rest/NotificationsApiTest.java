package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.settings.ScheduledTasksPage;
import com.tle.webtests.pageobject.tasklist.ModerationView;
import com.tle.webtests.pageobject.tasklist.NotificationsPage;
import com.tle.webtests.pageobject.tasklist.TaskListPage;
import com.tle.webtests.pageobject.viewitem.ShareWithOthersPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NotificationsApiTest extends AbstractItemApiTest {
  // @formatter:off
  private final String[][] MAP = {
    {"wentlive", "NotificationsApiTest - Other User Notifying You"},
    {
      "watchedwentlive",
      "NotificationsApiTest - Watched Collection",
      "NotificationsApiTest - Other User Notifying You"
    },
    {"rejected", "NotificationsApiTest - Rejected Item"},
    {"overdue", "NotificationsApiTest - Moderation Overdue"}
  };
  // @formatter:on

  private static final String OAUTH_CLIENT_ID = "NotificationsTestClient";

  private String notificationIdToDelete;

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void notificationsSubsearchTest() throws Exception {
    setupNotifications(); // Since we're clearing notifications we need to
    // create them each time
    waitForIndex();
    testGetNotifications();
    testClearNotifications();
  }

  private void waitForIndex() throws IOException {
    final String token = requestToken(OAUTH_CLIENT_ID);

    WebDriverWait notificationsIndexedWaiter = new WebDriverWait(context.getDriver(), 10);
    notificationsIndexedWaiter.until(
        (Function<WebDriver, Boolean>)
            driver -> {
              try {
                JsonNode resultsNode =
                    doNotificationsSearch(
                        "NotificationsApiTest", "all", ImmutableMap.of("order", "name"), token);
                return resultsNode.get("available").asInt() == 5;
              } catch (Exception e) {
                e.printStackTrace();
                return true;
              }
            });
  }

  public void setupNotifications() {
    // Pretty much ripped straight from NotificationsTest.java
    String REJECTED_ITEM = "NotificationsApiTest - Rejected Item";
    String OTHER_USER = "NotificationsApiTest - Other User Notifying You";
    String WATCHED_COLLECTION = "NotificationsApiTest - Watched Collection";
    String MOD_OVERDUE = "NotificationsApiTest - Moderation Overdue";

    logon("autotest", "automated");

    // Rejected
    WizardPageTab wizard = new ContributePage(context).load().openWizard("Basic Items (moderated)");
    wizard.editbox(1, REJECTED_ITEM);
    wizard.setCheck(3, "true", true);
    wizard.save().submit();
    TaskListPage taskList = new TaskListPage(context).load();
    ModerationView mv = taskList.exactQuery(REJECTED_ITEM).moderate(REJECTED_ITEM);
    mv.reject().rejectWithMessage("Boo", null);

    // Moderation Overdue
    wizard = new ContributePage(context).load().openWizard("Notification Collection");
    wizard.editbox(1, MOD_OVERDUE);
    wizard.save().submit();

    // Owner notifying you
    wizard = new ContributePage(context).load().openWizard("Basic Items");
    wizard.editbox(1, OTHER_USER);
    wizard.save().draft();
    ItemAdminPage filterListPage = new ItemAdminPage(context).load();
    SummaryPage summaryPage =
        filterListPage
            .all()
            .search("NotificationsApiTest")
            .getResultForTitle(OTHER_USER, 1)
            .viewSummary();
    ShareWithOthersPage swop = summaryPage.share();
    swop.selectUser("AutoTest", "AutoTest", "Auto Test");
    summaryPage.edit().save().publish();

    // Watched going live
    wizard = new ContributePage(context).load().openWizard("Basic Items");
    wizard.editbox(1, WATCHED_COLLECTION);
    wizard.save().publish();

    logout();

    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    ScheduledTasksPage stp = new ScheduledTasksPage(context).load();
    stp.runCheckModeration();
    stp.runNotifyNewTasks();
    stp.runCheckReview();

    new NotificationsPage(context).load();
  }

  private void testClearNotifications() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    HttpDelete delete =
        new HttpDelete(
            context.getBaseUrl()
                + "api/notification/"
                + notificationIdToDelete
                + "/?waitforindex=true");
    execute(delete, false, token);

    JsonNode resultsNode =
        doNotificationsSearch(
            "NotificationsApiTest",
            "all",
            ImmutableMap.of("order", "name", "showall", "true"),
            token);

    assertEquals(resultsNode.get("start").asInt(), 0);
    assertEquals(resultsNode.get("length").asInt(), 4);
    assertEquals(resultsNode.get("available").asInt(), 4);
  }

  private void testGetNotifications() throws Exception {
    final String token = requestToken(OAUTH_CLIENT_ID);

    for (String[] arr : MAP) {
      String uuid = "";
      int version = 0;
      JsonNode notificationsNode = null;

      JsonNode resultsNode =
          doNotificationsSearch(
              "NotificationsApiTest",
              arr[0],
              ImmutableMap.of("order", "name", "showall", "true"),
              token);

      try {
        notificationsNode = resultsNode.get("results");

        if (arr == MAP[0]) {
          notificationIdToDelete = notificationsNode.get(0).get("id").asText();
        }

        uuid = notificationsNode.get(0).get("item").get("uuid").asText();
        version = notificationsNode.get(0).get("item").get("version").asInt(1);
      } catch (NullPointerException e) {
        Assert.assertTrue(false, "Uuid or version not found in JSON: " + notificationsNode);
      }

      boolean matchFound = getItemName(uuid, version, token).equals(arr[1]);

      if (arr.length == 3) {
        // Notifications come in random order, don't bother checking
        // both
        matchFound = (matchFound || getItemName(uuid, version, token).equals(arr[2]));
      }
      assertTrue(matchFound, "The expected notification was not found");
    }
    JsonNode resultsNode =
        doNotificationsSearch(
            "NotificationsApiTest",
            "all",
            ImmutableMap.of("order", "name", "showall", "true"),
            token);

    assertEquals(resultsNode.get("start").asInt(), 0);
    assertEquals(resultsNode.get("length").asInt(), 5);
    assertEquals(resultsNode.get("available").asInt(), 5);
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    super.cleanupAfterClass();
    AbstractCleanupTest.deleteItemsWithPrefix(
        context, "autotest", "automated", "NotificationsApiTest");
  }

  private JsonNode doNotificationsSearch(
      String query, String subsearch, Map<?, ?> otherParams, String token) throws Exception {
    List<NameValuePair> params = Lists.newArrayList();
    if (query != null) {
      params.add(new BasicNameValuePair("q", query));
    }
    if (query != null) {
      params.add(new BasicNameValuePair("type", subsearch));
    }
    for (Entry<?, ?> entry : otherParams.entrySet()) {
      params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
    }
    String paramString = URLEncodedUtils.format(params, "UTF-8");
    HttpGet get = new HttpGet(context.getBaseUrl() + "api/notification?" + paramString);
    HttpResponse response = execute(get, false, token);
    JsonNode resultsNode = mapper.readTree(response.getEntity().getContent());

    // JsonNode notificationsNode = resultsNode.get("results");
    //
    // try
    // {
    // String uuid =
    // notificationsNode.get(0).get("item").get("uuid").asText();
    // int version =
    // notificationsNode.get(0).get("item").get("version").asInt(1);
    // }
    // catch( Exception e )
    // {
    // int a = 3; //Put breakpoint here to debug
    // int b = a + 1;
    // }
    return resultsNode;
  }

  private String getItemName(String uuid, int version, String token) throws Exception {
    HttpGet get = new HttpGet(context.getBaseUrl() + "api/item/" + uuid + "/" + version);
    HttpResponse response = execute(get, false, token);
    JsonNode jsonNode = mapper.readTree(response.getEntity().getContent());
    return jsonNode.get("name").asText();
  }
}
