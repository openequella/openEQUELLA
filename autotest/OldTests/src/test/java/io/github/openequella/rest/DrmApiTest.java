package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class DrmApiTest extends AbstractRestApiTest {
  private final String ITEM_UUID = "ea61a83c-b18b-49e2-8096-e6208776be92";
  private final String UNAUTHORISED_ITEM_UUID = "73b67c33-aa72-419f-87aa-72d919fcf9f0";
  private final int ITEM_VERSION = 1;

  @Override
  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(DrmApiTest.class);
    }
    return testConfig;
  }

  @Test(description = "Failed to accept DRM terms due to invalid Item ID")
  public void invalidItemId() throws IOException {
    assertEquals(acceptDrm("abc", ITEM_VERSION), 404);
  }

  @Test(description = "Failed to accept DRM terms due to unauthorised")
  public void unauthorised() throws IOException {
    assertEquals(acceptDrm(UNAUTHORISED_ITEM_UUID, ITEM_VERSION), 401);
  }

  @Test(description = "Successfully accept DRM terms")
  public void accept() throws IOException {
    assertEquals(acceptDrm(ITEM_UUID, ITEM_VERSION), 200);
  }

  @Test(
      description = "Fail to accept DRM terms due to already accepted",
      dependsOnMethods = "accept")
  public void alreadyAccepted() throws IOException {
    assertEquals(acceptDrm(ITEM_UUID, ITEM_VERSION), 400);
  }

  @Test(description = "Fail to accept DRM terms due to access denied", priority = 10)
  public void accessDenied() throws IOException {
    logout();
    assertEquals(acceptDrm(ITEM_UUID, ITEM_VERSION), 403);
  }

  @Test(description = "Successfully list DRM terms")
  public void listTerms() throws IOException {
    JsonNode result = listDrmTerms("677a4bbc-defc-4dc1-b68e-4e2473b66a6a", 1);
    JsonNode agreements = result.get("agreements");
    assertNotNull(agreements);
    assertNotNull(agreements.get("regularPermission"));
  }

  @Test(description = "List why the DRM Item is unauthorised to view")
  public void listDrmViolations() throws IOException {
    final GetMethod method =
        new GetMethod(buildEndpointPath(UNAUTHORISED_ITEM_UUID, ITEM_VERSION) + "/violations");
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);
  }

  @Test(description = "Failed to list DRM violations for authorised Item")
  public void listViolationsForAuthorisedItem() throws IOException {
    final GetMethod method =
        new GetMethod(buildEndpointPath(ITEM_UUID, ITEM_VERSION) + "/violations");
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 400);
  }

  private String buildEndpointPath(String uuid, int version) {
    return getTestConfig().getInstitutionUrl() + "api/item/" + uuid + "/" + version + "/drm";
  }

  private int acceptDrm(String uuid, int version) throws IOException {
    final PostMethod method = new PostMethod(buildEndpointPath(uuid, version));
    return makeClientRequest(method);
  }

  private JsonNode listDrmTerms(String uuid, int version) throws IOException {
    final GetMethod method = new GetMethod(buildEndpointPath(uuid, version));
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);

    return mapper.readTree(method.getResponseBodyAsStream());
  }
}
