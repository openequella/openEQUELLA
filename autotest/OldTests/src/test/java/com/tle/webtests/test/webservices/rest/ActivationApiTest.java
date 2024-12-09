package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.common.Pair;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

public class ActivationApiTest extends AbstractRestApiTest {
  private static final String API_TASK_PATH = "api/activation";
  private static final String OAUTH_CLIENT_ID = "ActivationApiTest";
  private static final String KNOWN_ITEM_UUID = "153e5049-c417-424c-be0b-d97145ae4276";
  private static final String KNOWN_ATTACHMENT_UUID = "1ad866d4-668e-43fd-bbd8-801b9b3a935f";
  private static final String KNOWN_COURSE_UUID = "0613456a-c723-44ce-b7f7-e2f2379fe07a";
  private static final String KNOWN_UNPOPULATED_COURSE = "badbad1d-dead-dead-dead-babb1eface00";
  private static final String DELETABLE_ACTIVATION_UUID_PREFIX = "ba5eba11-cafe-f00d-beef";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test(dependsOnMethods = {"testDeactivateAndReactivateActivation"})
  public void testRetrieveActivations() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);
    JsonNode result = getEntity(uri.toString(), token);
    // being wary of composing an unneeded string with a null pointer
    assertNull(
        result.get("error"),
        "Resonse unexpected: "
            + (result.get("error") != null ? result.get("error").asText() : "((pro forma))"));
    int resultNum = result.get("length").asInt();
    assertTrue(resultNum >= 1, "Expected at least 1");
    JsonNode resultNode =
        containsInSearchBeanResult(result, "apretendedpagefromapretendbook.txt", "description");
    assertNotNull(resultNode);

    // try it again, using the optional parameters. Expecting 'any' to be
    // identical to not having any params.
    // Expecting there be none in the persisted data under 'expired'
    result = getEntity(uri.toString(), token, "status", "any");
    int resultAny = result.get("length").asInt();
    assertTrue(
        resultNum == resultAny,
        "Expected 'any' to return (" + resultNum + ", same as <no param>) but we got " + resultAny);

    result = getEntity(uri.toString(), token, "status", "expired");
    int resultExpired = result.get("length").asInt();
    // We don't expect any expired activations in the persisted institution,
    // but there may be some from other test methods
    String uuids = "";
    for (int i = 0; i < resultExpired; ++i) {
      String expiredUuid = result.get("results").get(i).get("uuid").asText();
      uuids += expiredUuid + " ";
      // assertTrue(expiredUuid.startsWith(DELETABLE_ACTIVATION_UUID_PREFIX),
      // "unexpected uuid " + expiredUuid);
    }
    assertTrue(uuids.contains(DELETABLE_ACTIVATION_UUID_PREFIX));
    // query by course id
    result = getEntity(uri.toString(), token, "course", KNOWN_COURSE_UUID);
    int resultViaCourse = result.get("length").asInt();
    assertTrue(
        resultNum == resultViaCourse,
        "Expected 'course' to return ("
            + resultNum
            + ", same as <no param>) but we got "
            + resultViaCourse);

    // query by course id - when the course exists but has no Activations
    // associated
    result = getEntity(uri.toString(), token, "course", KNOWN_UNPOPULATED_COURSE);
    int resultViaUninvolvedCourse = result.get("length").asInt();
    assertTrue(
        resultViaUninvolvedCourse == 0,
        "Expected zero (enquiring on unpopulated course) but we got " + resultViaUninvolvedCourse);
  }

  @Test(dependsOnMethods = {"testRetrieveActivations"})
  public void testCreateAndDeleteActivation() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);

    final String IMPOSED_UUID = DELETABLE_ACTIVATION_UUID_PREFIX + "-0123456789ab";
    // NB: optionally setting our own UUID

    String jsonStr =
        "{ "
            + "\"attachment\": \""
            + KNOWN_ATTACHMENT_UUID
            + "\", "
            + "\"course\": { "
            + "\"uuid\": \""
            + KNOWN_COURSE_UUID
            + "\" "
            + " }, "
            + "\"description\": \"Senatus populusque romanus.txt\", ";

    jsonStr +=
        "\"item\": { "
            + "\"uuid\": \""
            + KNOWN_ITEM_UUID
            + "\", "
            + "\"version\": 1 "
            + "  }, "
            + "\"type\": \"cal\", "
            + "\"uuid\": \""
            + IMPOSED_UUID
            + "\" "
            + " } ";

    // JSON Date weirdness ...?
    ObjectNode jsonObj = (ObjectNode) mapper.readTree(jsonStr);
    GregorianCalendar whateverTodayIs = new GregorianCalendar();
    GregorianCalendar sixMonthsHence = (GregorianCalendar) whateverTodayIs.clone();
    sixMonthsHence.add(Calendar.MONTH, 6);

    jsonObj.put("from", whateverTodayIs.getTimeInMillis());
    jsonObj.put("until", sixMonthsHence.getTimeInMillis());
    jsonStr = jsonObj.toString();

    HttpResponse httpResponse = postEntity(jsonStr, uri.toString(), token, true);
    int responseCode = httpResponse.getStatusLine().getStatusCode();
    assertEquals(responseCode, 201, "Failed to create activation");
    String loc = httpResponse.getFirstHeader("Location").getValue();
    JsonNode result = getEntity(loc, token);
    String echoedUuid = result.get("uuid").asText();
    assertEquals(
        echoedUuid, IMPOSED_UUID, "The UUID we we got (" + echoedUuid + ") was not as expected.");

    String deleteUri = uri.toString() + "/" + IMPOSED_UUID;
    httpResponse = deleteResource(deleteUri, token);
    responseCode = httpResponse.getStatusLine().getStatusCode();
    // 204, NO_CONTENT - OK response for DELETE
    assertEquals(responseCode, 204, "Failed to delete activation");
  }

  @Test
  public void testDeactivateAndReactivateActivation() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);

    final String IMPOSED_UUID = DELETABLE_ACTIVATION_UUID_PREFIX + "-ba9876543210";

    Calendar whateverYesterdayIs = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
    Calendar tomorrow = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
    whateverYesterdayIs.add(Calendar.DATE, -1);
    tomorrow.add(Calendar.DATE, 1);
    // NB: Sending our own UUID
    String jsonStr =
        "{ "
            + "\"attachment\": \""
            + KNOWN_ATTACHMENT_UUID
            + "\", "
            + "\"course\": { "
            + "\"uuid\": \""
            + KNOWN_COURSE_UUID
            + "\" "
            + " }, "
            + "\"description\": \"Senatus populusque romanus.txt\", ";

    jsonStr +=
        "\"item\": { "
            + "\"uuid\": \""
            + KNOWN_ITEM_UUID
            + "\", "
            + "\"version\": 1 "
            + "  }, "
            + "\"type\": \"cal\", "
            + "\"uuid\": \""
            + IMPOSED_UUID
            + "\""
            + " } ";

    ObjectNode jsonObj = (ObjectNode) mapper.readTree(jsonStr);
    jsonObj.put("from", whateverYesterdayIs.getTimeInMillis());
    jsonObj.put("until", tomorrow.getTimeInMillis());
    jsonStr = jsonObj.toString();

    HttpResponse httpResponse = postEntity(jsonStr, uri.toString(), token, true);
    int responseCode = httpResponse.getStatusLine().getStatusCode();
    assertEquals(responseCode, 201, "Failed to create activation.");
    String loc = httpResponse.getFirstHeader("Location").getValue();

    JsonNode result = getEntity(loc, token);
    String echoedStatus = result.get("status").asText();
    assertEquals(echoedStatus, "active");

    // do a GET based on item uuid & version
    String getItemActivationUrl = uri.toString() + "/item/" + KNOWN_ITEM_UUID + "/1";
    result = getEntity(getItemActivationUrl, token);
    assertTrue(result.size() >= 1);

    JsonNode readOnlyNode = findNodeWithUuid(result, IMPOSED_UUID);
    // Send back the node contents, with a query param to de-activate
    assertEquals(readOnlyNode.get("uuid").asText(), IMPOSED_UUID);

    String targetUrl = uri.toString() + '/' + IMPOSED_UUID;

    httpResponse = getPut(targetUrl + "?disable=true", readOnlyNode, token);

    // We're expecting 200 ok for successful edit
    responseCode = httpResponse.getStatusLine().getStatusCode();
    assertEquals(responseCode, 200, "Failed to deactivate.");

    result = getEntity(uri.toString(), token, "status", "expired", "course", KNOWN_COURSE_UUID);
    boolean foundUuid = false;
    for (int i = 0; i < result.get("length").asInt(); ++i) {
      foundUuid |= result.get("results").get(i).get("uuid").asText().equals(IMPOSED_UUID);
    }
    assertTrue(foundUuid, "expected search for expired activations to return " + IMPOSED_UUID);
  }

  /**
   * Look through a list of object for one with a matching uuid property
   *
   * @param json
   * @param uuid
   * @return null if no match
   */
  private JsonNode findNodeWithUuid(JsonNode json, String uuid) {
    int available = json.size();

    for (int i = 0; i < available; i++) {
      if (json.get(i).get("uuid").asText().equals(uuid)) {
        return json.get(i);
      }
    }
    return null;
  }

  @Override
  public void cleanupAfterClass() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);
    JsonNode result = getEntity(uri.toString(), token);
    int resultNum = result.get("length").asInt();

    for (int i = 0; i < resultNum; ++i) {
      String uuid = result.get("results").get(i).get("uuid").asText();
      if (uuid.startsWith(DELETABLE_ACTIVATION_UUID_PREFIX)) {
        String targetUrl = uri.toString() + '/' + uuid;
        deleteResource(targetUrl, token);
      }
    }
    super.cleanupAfterClass();
  }
}
