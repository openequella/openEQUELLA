package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.common.Pair;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.testng.annotations.Test;

public class TasksApiTest extends AbstractRestApiTest {
  private final String API_TASK_PATH = "api/task";
  // @formatter:off
  private final String[][]
      MAP = { // The tasks call won't get item names so we need to match on UUID
    {"assignedme", "d7f554ee-d50f-4567-a85e-5e62578d10ed"},
    {"assignedothers", "9424a318-0c16-4080-a737-dc141cd46837"},
    {
      "assignednone",
      "ed883bc3-0a7a-48c4-acac-4b1e3c038f47",
      "24b977ec-4df4-4a43-8922-8ca6f82a296a",
      "e48edefa-e1fc-4ebe-946f-ff65938c9fae",
      "d6518347-366b-46db-8b53-327e7381a1e6",
      "cc7a3e13-4f44-4481-8bc2-881cecfbed96"
    },
    {
      "mustmoderate",
      "ed883bc3-0a7a-48c4-acac-4b1e3c038f47",
      "24b977ec-4df4-4a43-8922-8ca6f82a296a",
      "e48edefa-e1fc-4ebe-946f-ff65938c9fae",
      "d6518347-366b-46db-8b53-327e7381a1e6",
      "cc7a3e13-4f44-4481-8bc2-881cecfbed96"
    }
  };
  // @formatter:on

  private static final String OAUTH_CLIENT_ID = "TasksApiTestClient";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  private JsonNode doTasksSearch(
      String query, String subsearch, Map<?, ?> otherParams, String token) throws Exception {
    List<NameValuePair> params = Lists.newArrayList();
    if (query != null) {
      params.add(new BasicNameValuePair("q", query));
    }
    if (query != null) {
      params.add(new BasicNameValuePair("filter", subsearch));
    }
    params.add(new BasicNameValuePair("length", "100"));
    for (Entry<?, ?> entry : otherParams.entrySet()) {
      params.add(new BasicNameValuePair(entry.getKey().toString(), entry.getValue().toString()));
    }
    String paramString = URLEncodedUtils.format(params, "UTF-8");
    HttpGet get = new HttpGet(context.getBaseUrl() + API_TASK_PATH + "?" + paramString);
    HttpResponse response = execute(get, false, token);
    return mapper.readTree(response.getEntity().getContent());
  }

  @Test
  public void taskSubsearchTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    for (String[] arr : MAP) {
      JsonNode resultsNode =
          doTasksSearch("TaskApiTest", arr[0], ImmutableMap.of("order", "name"), token);

      for (int i = 1; i < arr.length; i++) {
        assertTrue(containsInAvailableResult(resultsNode, arr[i], "uuid"));
      }
    }
    JsonNode resultsNode =
        doTasksSearch("TaskApiTest", "all", ImmutableMap.of("order", "name"), token);

    assertEquals(resultsNode.get("start").asInt(), 0);
    assertEquals(resultsNode.get("length").asInt(), 7);
    assertEquals(resultsNode.get("available").asInt(), 7);
  }

  @Test
  public void taskSortTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    JsonNode resultsNode =
        doTasksSearch("TaskApiTest", "all", ImmutableMap.of("order", "name"), token);
    assertInNameOrder(resultsNode, token);
    resultsNode = doTasksSearch("", "all", ImmutableMap.of("order", "priority"), token);
    assertInPriorityOrder(resultsNode);
    resultsNode = doTasksSearch("", "all", ImmutableMap.of("order", "duedate"), token);
    assertInDueDateOrder(resultsNode);
    resultsNode = doTasksSearch("", "all", ImmutableMap.of("order", "waiting"), token);
    assertInWaitingOrder(resultsNode);
  }

  @Test
  public void taskFilterTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    JsonNode resultsNode =
        doTasksSearch(
            "TaskApiTest",
            "all",
            ImmutableMap.of("collections", "6bf51943-9d20-4934-a228-8edcec6691ae"),
            token);
    // Complex workflow collection
    assertTrue(
        containsInAvailableResult(resultsNode, "24b977ec-4df4-4a43-8922-8ca6f82a296a", "uuid"));
    assertTrue(
        containsInAvailableResult(resultsNode, "cc7a3e13-4f44-4481-8bc2-881cecfbed96", "uuid"));
    assertFalse(
        containsInAvailableResult(resultsNode, "9424a318-0c16-4080-a737-dc141cd46837", "uuid"));
  }

  /**
   * @throws Exception
   */
  @Test
  public void taskFilterCountsTest() throws Exception {
    // An expectation is keyed by tasktype, with a target number and a
    // boolean result (false until reset to true)
    Map<String, Pair<Integer, Boolean>> expectations =
        new HashMap<String, Pair<Integer, Boolean>>();

    // @formatter:off
    expectations.put("taskall", new Pair<Integer, Boolean>(8, false));
    expectations.put("taskme", new Pair<Integer, Boolean>(1, false));
    expectations.put("taskothers", new Pair<Integer, Boolean>(1, false));
    expectations.put("tasknoone", new Pair<Integer, Boolean>(6, false));
    expectations.put("taskmust", new Pair<Integer, Boolean>(6, false));
    expectations.put("noteall", new Pair<Integer, Boolean>(2, false));
    expectations.put("noteoverdue", new Pair<Integer, Boolean>(2, false));
    // @formatter:off

    String token = requestToken(OAUTH_CLIENT_ID);

    HttpGet get = new HttpGet(context.getBaseUrl() + API_TASK_PATH + "/filter?includeCounts=false");
    HttpResponse response = execute(get, false, token);
    assertEquals(
        response.getStatusLine().getStatusCode(),
        200,
        "Did not receive successful response from api/task/filtername");
    JsonNode filtersNode = mapper.readTree(response.getEntity().getContent());
    int numTaskTypes = filtersNode.get("results").size(); //  == 12, presumably

    assertTrue(
        numTaskTypes >= 7,
        "test assumes at least 7 task types, but recognise only " + numTaskTypes);

    get = new HttpGet(context.getBaseUrl() + API_TASK_PATH + "/filter?includeCounts=true");
    response = execute(get, false, token);

    JsonNode resultsNode = mapper.readTree(response.getEntity().getContent());
    JsonNode elemsNode = resultsNode.get("results");

    for (int i = 0; i < numTaskTypes; ++i) {
      JsonNode elemNode = elemsNode.get(i);
      String taskId = elemNode.get("id").asText();
      int count = elemNode.get("count").asInt();

      Pair<Integer, Boolean> expects = expectations.get(taskId);
      if (expects != null) {
        // the expectation exists: does the result match our assumption?
        assertTrue(
            expects.getFirst().compareTo(count) <= 0,
            "expected at least " + expects.getFirst() + " for " + taskId + "' but got " + count);
        // record success of the expectation
        expects.setSecond(true);
      }
    }

    // ensure all expectations were hit
    for (String key : expectations.keySet()) {
      assertTrue(expectations.get(key).getSecond(), "Neglected to affirm " + key);
    }
  }

  private void assertInNameOrder(JsonNode jsonNode, String token) throws Exception {
    int results = jsonNode.get("available").asInt();
    String first = "aaa"; // It'll do

    for (int i = 0; i < results; i++) {
      String name =
          getName(
              jsonNode.get("results").get(i).get("item").get("uuid").asText(),
              jsonNode.get("results").get(i).get("item").get("version").asInt(),
              token);
      assertTrue(name.compareToIgnoreCase(first) > 0);
      first = name;
    }
  }

  private void assertInPriorityOrder(JsonNode jsonNode) throws Exception {
    int results = jsonNode.get("available").asInt();
    int first = 90000; // It'll do

    for (int i = 0; i < results; i++) {
      int priority = jsonNode.get("results").get(i).get("task").get("priority").asInt();
      assertTrue(priority <= first);
      first = priority;
    }
  }

  private void assertInDueDateOrder(JsonNode jsonNode) throws Exception {
    int results = jsonNode.get("available").asInt();
    Date first = new Date(-3177517600000l); // 1869

    for (int i = 0; i < results; i++) {
      JsonNode dateNode = jsonNode.get("results").get(i).get("dueDate");
      if (dateNode == null) {
        first = new Date(2298153600000l); // 2042
        continue;
      }
      Date due = ISO8601Utils.parse(dateNode.asText(), new ParsePosition(0));
      assertTrue(
          due.after(first) || due.equals(first),
          "Results were not sorted by due date: " + first + " before " + due);
      first = due;
    }
  }

  private void assertInWaitingOrder(JsonNode jsonNode) throws Exception {
    int results = jsonNode.get("available").asInt();
    Date first = new Date(0l); // 1970

    for (int i = 0; i < results; i++) {
      JsonNode dateNode = jsonNode.get("results").get(i).get("startDate");
      Date started = ISO8601Utils.parse(dateNode.asText(), new ParsePosition(0));
      assertTrue(
          started.after(first) || started.equals(first), "Results were not sorted by waiting date");
      first = started;
    }
  }

  private String getName(String uuid, int version, String token) throws Exception {
    HttpGet get = new HttpGet(context.getBaseUrl() + "api/item/" + uuid + "/" + version);
    HttpResponse response = execute(get, false, token);
    JsonNode node = mapper.readTree(response.getEntity().getContent());
    return node.get("name").asText();
  }

  // A bit crude, but should do the trick
  private boolean containsInAvailableResult(JsonNode json, String lookFor, String lookIn) {
    int available = json.get("available").asInt();
    JsonNode results = json.get("results");

    for (int i = 0; i < available; i++) {
      if (results.get(i).get("item").get(lookIn).asText().equals(lookFor)) {
        return true;
      }
    }

    return false;
  }
}
