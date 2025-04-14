package com.tle.webtests.test.webservices.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.common.Pair;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;

public class CourseApiTest extends AbstractRestApiTest {
  private static final String API_TASK_PATH = "api/course";

  /** Course to test for initially */
  private static final String EXISTING_EQ1001 = "EQ-1001";

  private static final String OAUTH_CLIENT_ID = "CourseApiTestClient";

  /**
   * Using the OAUTH_CLIENT_ID as recognisable (and hence disposable) temporary course name prefix
   */
  private static final String COURSE_CODE_RECOGNISABLE = OAUTH_CLIENT_ID + "-ZEPPLIN";

  private static final String COURSE_CODE_SRM114 = OAUTH_CLIENT_ID + "-SRM114";
  private static final String COURSE_CODE_INNOCENT = OAUTH_CLIENT_ID + "-INNOCENT";
  private static final String KNOWN_USER_UUID = "adfcaf58-241b-4eca-9740-6a26d1c3dd58";

  @Override
  protected void addOAuthClients(List<Pair<String, String>> clients) {
    clients.add(new Pair<String, String>(OAUTH_CLIENT_ID, "AutoTest"));
  }

  @Test
  public void testRetrieveCourses() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);
    JsonNode result = getEntity(uri.toString(), token);
    assertNotNull(containsInSearchBeanResult(result, EXISTING_EQ1001, "code"));
  }

  @Test(dependsOnMethods = {"testRetrieveCourses"})
  public void testCreateCourse() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);

    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);

    JsonNode newObj =
        createAndVerifyCourse(token, COURSE_CODE_RECOGNISABLE, "delete me if you see me", uri);

    String newUuid = newObj.get("uuid").asText();
    assertNotNull(newUuid);

    // retrieve the freshly created course
    String apiPlusUuid = uri.toString() + "/" + newUuid;
    JsonNode postGetNode = getEntity(apiPlusUuid, token);
    assertEquals(postGetNode.get("code").asText(), COURSE_CODE_RECOGNISABLE);

    int purged = purge(token);
    assertEquals(purged, 1, "Expected to purge 1.");
  }

  @Test(dependsOnMethods = {"testCreateCourse"})
  public void testCannotRePOSTSameCode() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    URI targetUri = new URI(context.getBaseUrl() + API_TASK_PATH);

    JsonNode firstObj =
        createAndVerifyCourse(
            token, COURSE_CODE_RECOGNISABLE, "delete me if you see me", targetUri);

    assertTrue(firstObj != null && firstObj.get("code").asText().equals(COURSE_CODE_RECOGNISABLE));

    String jsonStr =
        buildJsonCourse(
            COURSE_CODE_RECOGNISABLE,
            "We expect it's a folorn hope for this object to be created",
            "2010-03-31",
            "2010-12-25",
            31,
            KNOWN_USER_UUID,
            null);

    HttpResponse postResponse = postEntity(jsonStr, targetUri.toString(), token, false);
    int postResponseCode = postResponse.getStatusLine().getStatusCode();
    // 400 (bad request) 500 (internal server error)
    assertTrue(
        postResponseCode == 400 || postResponseCode == 500,
        "unexpected (" + postResponseCode + ") post response.");

    int purged = purge(token);
    assertEquals(purged, 1, "Expected to purge 1.");
  }

  /*
  	@Test(dependsOnMethods={"testCannotRePOSTSameCode"})
  	public void testPUTCannotOverwriteCodeInUse() throws Exception
  	{
  		String token = requestToken(OAUTH_CLIENT_ID);
  		URI targetUri = new URI(context.getBaseUrl() + API_TASK_PATH);

  		JsonNode youAgainNode = createAndVerifyCourse(token, COURSE_CODE_SRM114,
  				"in order to attempt to edit", targetUri);

  		assertTrue(youAgainNode != null && youAgainNode.get("code").asText().equals(COURSE_CODE_SRM114));

  		JsonNode innocentBystanderCourse = createAndVerifyCourse(token, COURSE_CODE_INNOCENT,
  				"in order to attempt to edit", targetUri);

  		String innocentTrueUuid = innocentBystanderCourse.get("uuid").asText();

  		// we going to use the 'edit' API but sending a JSON object which purports to set a
  		// different uuid, (which would be legal) but attempting to use a code which we know
  		// already exists
  		ObjectNode innocentNodeReally = (ObjectNode)innocentBystanderCourse;
  		innocentNodeReally.put("code", COURSE_CODE_SRM114);
  		innocentNodeReally.put("name", "something seen nowhere else");
  		innocentNodeReally.put("description", "Oh it was gorgeousness and gorgeosity made flesh. "
  				+ "The trombones crunched redgold under my bed, and behind my gulliver the trumpets three-wise silverflamed, "
  				+ "and there by the door the timps rolling through my guts and out again crunched like candy thunder. "
  				+ "Oh, it was wonder of wonders. And then, a bird of like rarest spun heavenmetal, "
  				+ "or like silvery wine flowing in a spaceship, gravity all nonsense now, came the violin solo above all the other strings, "
  				+ "and those strings were like a cage of silk round my bed. Then flute and oboe bored, like worms of like platinum, "
  				+ "into the thick thick toffee gold and silver. I was in such bliss, my brothers."); // Anthony Burgess: A clockwork orange
  		innocentNodeReally.put("from", "2010-03-31");
  		innocentNodeReally.put("until", "2010-12-25");
  		innocentNodeReally.put("students", 31);
  		innocentNodeReally.put("uuid", "deadbeef-f00d-cafe-feed-f0fffadef0ff");

  		String editUrl = targetUri.toString();
  		// I'm purporting to edit the Course created with the code COURSE_CODE_INNOCENT,
  		// by identifying it with its UUID. When editing I attempt to change its code to one I know
  		// already exists (the COURSE_CODE_SRM114). This should fail.
  		editUrl += "/" + innocentTrueUuid;
  		HttpResponse didIorDidntI = getPut(editUrl, innocentBystanderCourse, token);
  		int postResponseCode = didIorDidntI.getStatusLine().getStatusCode();
  		// 400 (bad request) 500 (internal server error)
  		assertTrue(postResponseCode == 400 || postResponseCode == 500, "unexpected (" + postResponseCode + ") post response");

  		int purged = purge(token);
  		assertEquals( purged, 2, "Expected to purge 2.");
  	}
  */
  private JsonNode createAndVerifyCourse(String token, String code, String name, URI uri)
      throws Exception {
    String jsonStr =
        buildJsonCourse(code, name, "2014-03-31", "2014-12-25", 31, KNOWN_USER_UUID, null);

    HttpResponse postResponse = postEntity(jsonStr, uri.toString(), token, true);
    int postResponseCode = postResponse.getStatusLine().getStatusCode();
    // 200 (success) or 201 (created)
    assertTrue(
        postResponseCode == 200 || postResponseCode == 201,
        "nonsuccess (" + postResponseCode + ") in post response.");

    String loc = postResponse.getFirstHeader("Location").getValue();
    return getEntity(loc, token);
  }

  private String buildJsonCourse(
      String code,
      String name,
      String dateFromStr,
      String dateUntilStr,
      int students,
      String ownerUuid,
      String uuid)
      throws Exception {
    String jsonStr = "{ ";
    if (uuid != null) {
      jsonStr += "\"uuid\": \"" + uuid + "\",";
    }
    jsonStr += "\"code\": \"" + code + "\",";
    jsonStr += "\"name\": \"" + name + "\",";
    jsonStr += "\"type\": \"Internal\",";
    jsonStr += "\"citation\": \"Generic\",";
    jsonStr += "\"departmentName\": \"who cares?\",";
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.parse(dateFromStr); // sanity check: tests parseability
    jsonStr += "\"from\":\"" + dateFromStr + "\",";
    dateFormat.parse(dateUntilStr); // ditto
    jsonStr += "\"until\":\"" + dateUntilStr + "\",";
    jsonStr += "\"students\":" + students + ",";
    jsonStr += "\"owner\": { \"id\":\"" + ownerUuid + "\" } ";
    jsonStr += "}";

    return jsonStr;
  }

  protected int purge(String token) throws Exception {
    int purged = 0;
    URI uri = new URI(context.getBaseUrl() + API_TASK_PATH);
    JsonNode result = getEntity(uri.toString(), token);
    if (result.get("length").asInt() > 0) {
      JsonNode allResults = result.get("results");
      for (int i = 0; i < allResults.size(); ++i) {
        JsonNode aResult = allResults.get(i);
        if (aResult.get("code").asText().startsWith(OAUTH_CLIENT_ID)) {
          String uuidToDelete = aResult.get("uuid").asText();
          deleteResource(uri.toString() + '/' + uuidToDelete, token);
          ++purged;
        }
      }
    }
    return purged;
  }

  @Override
  @SuppressWarnings("unused")
  public void cleanupAfterClass() throws Exception {
    String token = requestToken(OAUTH_CLIENT_ID);
    int dontcareanymore = purge(token);
    super.cleanupAfterClass();
  }
}
