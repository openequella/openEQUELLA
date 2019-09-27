package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.tle.common.Check;
import com.tle.common.Pair;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 'REST testing' institution assumptions: there's 9 users, 5 groups
 *
 * @author larry
 */
public class UserGroupManagementApiTest extends AbstractRestApiTest {
  private static final String API_PATH = "api/usermanagement/local/";
  private static final String API_PATH_USER = API_PATH + "user/";
  private static final String API_PATH_GROUP = API_PATH + "group/";
  private static final String OAUTH_CLIENT_ID = "UserGroupManagementApiTestClient";
  private static final String API_PATH_USER_INFO_BACKUP = "api/userquery/userinfobackup/";

  private static final int NUM_USERS = 9;
  private static final int NUM_GROUPS = 5;

  private static final String AUTOTEST_LOGON = "AutoTest";
  private static final String AUTOTEST_LAST_NAME = "Test";
  private static final String AUTOTEST_FIRST_NAME = "Auto";
  private static final String AUTOTEST_EMAIL = "auto@test.com";
  private static final String AUTOTEST_USER_UUID = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";
  private static final String TOKENUSER_LOGON = "tokenuser";
  private static final String UNIQUE_ID = "uniqueId";
  private static final String USER_NAME = "username";
  private static final String LAST_NAME = "lastName";
  private static final String FIRST_NAME = "firstName";
  private static final String EMAIL_ADDRESS = "emailAddress";
  private static final String TOKENUSER_UUID = "7e296f6f-8880-43a7-b00b-e42c6b816a1d";
  private static final String FLATGROUP = "SelectGroup0";
  private static final String SUPERGROUP = "SelectGroup1";
  // tokenuser is a member of the subgroup
  private static final String SUBGROUP = "SelectSubGroup1";
  private static final String COMMON_SUBSTRING = FLATGROUP.substring(0, 6);

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void userInfoBackupTest() throws IOException {
    JsonNode userResult =
        getEntity(
            context.getBaseUrl() + API_PATH_USER_INFO_BACKUP, null, UNIQUE_ID, AUTOTEST_USER_UUID);
    String username = userResult.get(USER_NAME).asText();
    assertEquals(username, AUTOTEST_LOGON);
    String lastName = userResult.get(LAST_NAME).asText();
    assertEquals(lastName, AUTOTEST_LAST_NAME);
    String firstName = userResult.get(FIRST_NAME).asText();
    assertEquals(firstName, AUTOTEST_FIRST_NAME);
    String email = userResult.get(EMAIL_ADDRESS).asText();
    assertEquals(email, AUTOTEST_EMAIL);
  }

  @Test
  public void userSearchAndEditTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    // search for known user
    JsonNode userResult =
        getEntity(context.getBaseUrl() + API_PATH_USER + AUTOTEST_USER_UUID, token);
    assertEquals(userResult.get("username").asText(), AUTOTEST_LOGON);

    // Dredge up a user and edit them
    JsonNode userNode = getEntity(context.getBaseUrl() + API_PATH_USER + TOKENUSER_UUID, token);
    String userName = userNode.get("username").asText();
    assertEquals(TOKENUSER_LOGON, userName, "User with uuid " + TOKENUSER_UUID + " mismatch");
    assertNull(userNode.get("emailAddress"));

    // cast the node into its editable form, set an email and change the
    // password
    ObjectNode userNodeReally = (ObjectNode) userNode;
    String anEmail = "piper@the.gates.of.da.wn";
    userNodeReally.put("emailAddress", anEmail);
    userNodeReally.put("password", "Not6backtix");

    HttpResponse putResponse =
        getPut(context.getBaseUrl() + API_PATH_USER + TOKENUSER_UUID, userNodeReally, token);
    int respCode = putResponse.getStatusLine().getStatusCode();
    assertTrue(respCode == 200, "Put unsuccessful");
    ByteArrayOutputStream boas = new ByteArrayOutputStream();
    putResponse.getEntity().writeTo(boas);
    String str = new String(boas.toByteArray(), "UTF-8");
    assertEquals(str, TOKENUSER_UUID);

    // get the user via their name as query arg, and expunge the email and
    // restore the old password, for next time

    JsonNode resultsNode =
        getEntity(context.getBaseUrl() + API_PATH_USER, token, "q", TOKENUSER_LOGON);
    assertEquals(
        resultsNode.get("available").asInt(), 1, "Expected exactly one '" + TOKENUSER_LOGON + "'");
    assertEquals(resultsNode.get("results").get(0).get("id").asText(), TOKENUSER_UUID);
    userNodeReally = (ObjectNode) resultsNode.get("results").get(0);

    String selfLink = userNodeReally.get("links").get("self").asText();
    userNodeReally = (ObjectNode) getEntity(selfLink, token);
    assertEquals(userNodeReally.get("emailAddress").asText(), anEmail);
    userNodeReally.remove("emailAddress");
    userNodeReally.put("password", "``````");

    // put the re-edited node back in
    putResponse =
        getPut(context.getBaseUrl() + API_PATH_USER + TOKENUSER_UUID, userNodeReally, token);
    respCode = putResponse.getStatusLine().getStatusCode();
    assertTrue(respCode == 200, "re-Put unsuccessful");
  }

  @Test
  public void groupSearchTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    // 3 groups name contains 'Select'
    JsonNode selectedGroupsResponse = doQuerySearch(API_PATH_GROUP, "Select", null, token);
    assertEquals(
        selectedGroupsResponse.get("available").asInt(), 3, "Groups matching 'Select' available");
    JsonNode selectedGroupsResult = selectedGroupsResponse.get("results");
    assertEquals(selectedGroupsResult.size(), 3, "Groups matching 'Select'");

    // token user not in this group ...
    JsonNode notinhereResponse =
        doQuerySearch(
            API_PATH_GROUP,
            SUPERGROUP,
            ImmutableMap.of("user", TOKENUSER_UUID, "allParents", "false"),
            token);
    JsonNode notinhere = notinhereResponse.get("available");
    assertEquals(notinhere.asInt(), 0, "tokenuser (directly) in " + SUPERGROUP);

    // token user not in this group either...
    JsonNode notinhereEitherResponse =
        doQuerySearch(API_PATH_GROUP, FLATGROUP, ImmutableMap.of("user", TOKENUSER_UUID), token);
    JsonNode notinhereEither = notinhereEitherResponse.get("available");
    assertEquals(notinhereEither.asInt(), 0, "tokenuser in " + FLATGROUP);

    // but tokenuser is in a group descended from supergroup, when we allow
    // upwards recursion (aka allparents)
    JsonNode inhereindirectly =
        doQuerySearch(API_PATH_GROUP, SUPERGROUP, ImmutableMap.of("user", TOKENUSER_UUID), token);
    assertEquals(
        inhereindirectly.get("available").asInt(), 1, "tokenuser (in directly) in " + SUPERGROUP);

    // ... but the supergroup is included as groups of which token is a
    // member. NB: upwards recursion defaults to true
    JsonNode butisinhere =
        doQuerySearch(
            API_PATH_GROUP, COMMON_SUBSTRING, ImmutableMap.of("user", TOKENUSER_UUID), token);
    assertEquals(butisinhere.get("available").asInt(), 2, "groups including tokenuser");
    assertTrue(
        groupArrayContainsElement(butisinhere.get("results"), "name", SUPERGROUP),
        "Group includes supergroup");
    assertTrue(
        groupArrayContainsElement(butisinhere.get("results"), "name", SUBGROUP),
        "Group includes subgroup");

    // without upwards recursion, tokenuser is only a member of one group
    JsonNode onlyOneGroup =
        doQuerySearch(
            API_PATH_GROUP,
            null,
            ImmutableMap.of("user", TOKENUSER_UUID, "allParents", "false"),
            token);
    assertEquals(onlyOneGroup.get("available").asInt(), 1, "tokenuser's solitary group");
  }

  public void pagingQueryTestNotUsed() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    doPagingTest(API_PATH_USER, NUM_USERS, 3, token);

    doPagingTest(API_PATH_GROUP, NUM_GROUPS, 3, token);
  }

  private void doPagingTest(
      String apiPath, int totalThatExists, int skipOverFromStart, String token) throws Exception {
    // plain item (user or groups) search
    JsonNode userResponse = doQuerySearch(apiPath, null, null, token);
    assertEquals(userResponse.get("available").asInt(), totalThatExists, "Items available");
    JsonNode userResults = userResponse.get("results");
    assertEquals(userResults.size(), totalThatExists, "Users results total");

    Map<?, ?> otherParams =
        ImmutableMap.of(
            "start",
            Integer.toString(skipOverFromStart),
            "length",
            Integer.toString(totalThatExists));
    userResponse = doQuerySearch(apiPath, null, otherParams, token);
    // expecting the same availability
    assertEquals(userResponse.get("available").asInt(), totalThatExists, "Items available");
    // but shorter length than we asked for
    assertEquals(
        userResponse.get("length").asInt(),
        totalThatExists - skipOverFromStart,
        "Expected to skip over " + skipOverFromStart);

    otherParams = ImmutableMap.of("length", "0");
    userResponse = doQuerySearch(apiPath, null, otherParams, token);
    assertEquals(userResponse.get("available").asInt(), totalThatExists, "Items available");
    assertEquals(userResponse.get("length").asInt(), 0);
    userResults = userResponse.get("results");
    assertTrue(userResults == null || userResults.size() == 0, "Asked for 0 results");
  }

  @Test
  public void addAndDeleteGroupTest() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    String stringified = "{\"name\":\"" + this.getClass().getSimpleName() + "-grp1\"}";

    HttpPost post = getPost(context.getBaseUrl() + API_PATH_GROUP, stringified);
    HttpResponse response = execute(post, true, token);
    // 201 - creation accepted
    assertEquals(
        response.getStatusLine().getStatusCode(), 201, "Unexpected HTTP response from POST");

    Header loc = response.getFirstHeader("Location");
    Assert.assertNotNull(loc, "No location header on group creation response");

    // get all groups created by this test
    JsonNode eponymousGroups =
        doQuerySearch(API_PATH_GROUP, this.getClass().getSimpleName(), null, token);
    int howManyGroupsToDelete = eponymousGroups.get("available").asInt();
    JsonNode results = eponymousGroups.get("results");

    int deleted = 0;
    while (deleted < howManyGroupsToDelete) {
      JsonNode group = results.get(deleted);

      // sanity check
      Assert.assertTrue(
          group.get("name").asText().contains(this.getClass().getSimpleName()),
          "Group search returned a result it shouldn't have!");

      String selfLink = group.get("links").get("self").asText();
      HttpResponse deletionResponse = deleteResource(selfLink, token);

      int deletionCode = deletionResponse.getStatusLine().getStatusCode();
      // 204 : NO_CONTENT - to indicate deletion accepted
      assertTrue(
          deletionCode == 204,
          "Failed to delete group with name: "
              + group.get("name").asText()
              + " - "
              + deletionCode
              + " ("
              + deletionResponse.getStatusLine().getReasonPhrase()
              + ")");
      ++deleted;
    }

    assertTrue(deleted > 0, "Expected to delete " + howManyGroupsToDelete + " groups");
  }

  /**
   * @param incompleteApiPath the path WITHOUT the server name
   * @param query
   * @param otherParams
   * @param token
   * @return
   * @throws Exception
   */
  private JsonNode doQuerySearch(
      String incompleteApiPath, String query, Map<?, ?> otherParams, String token)
      throws Exception {
    List<String> params = Lists.newArrayList();
    if (query != null) {
      params.add("q");
      params.add(query);
    }
    if (!Check.isEmpty(otherParams)) {
      for (Entry<?, ?> entry : otherParams.entrySet()) {
        params.add(entry.getKey().toString());
        params.add(entry.getValue().toString());
      }
    }

    String apiPath = context.getBaseUrl() + incompleteApiPath;
    JsonNode retNode = getEntity(apiPath, token, params.toArray());
    return retNode;
  }

  private int addEntity(String apiPath, String jsonEntity, String token) throws Exception {
    HttpPost post = getPost(context.getBaseUrl() + apiPath, jsonEntity);
    HttpResponse response = execute(post, true, token);
    return response.getStatusLine().getStatusCode();
  }

  private boolean groupArrayContainsElement(JsonNode collection, String property, String target) {
    int elemNumber = collection.size();
    while (--elemNumber >= 0) {
      if (collection.get(elemNumber).get(property).asText().equals(target)) {
        return true;
      }
    }
    return false;
  }
}
