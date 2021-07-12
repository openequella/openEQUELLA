package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.tle.webtests.framework.TestConfig;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.JsonNode;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class DrmApiTest extends AbstractRestApiTest {
  private final String DRM_API_ENDPOINT = getTestConfig().getInstitutionUrl() + "api/drm/";
  private final String ITEM_UUID = "ea61a83c-b18b-49e2-8096-e6208776be92";

  @Override
  protected TestConfig getTestConfig() {
    if (testConfig == null) {
      testConfig = new TestConfig(DrmApiTest.class);
    }
    return testConfig;
  }

  @Test(description = "Failed to accept DRM terms due to invalid Item ID")
  public void invalidItemId() throws IOException {
    assertEquals(acceptDrm("abc", 1), 404);
  }

  @Test(description = "Failed to accept DRM terms due to unauthorised")
  public void unauthorised() throws IOException {
    assertEquals(acceptDrm("73b67c33-aa72-419f-87aa-72d919fcf9f0", 1), 401);
  }

  @Test(description = "Successfully accept DRM terms")
  public void accept() throws IOException {
    assertEquals(acceptDrm(ITEM_UUID, 1), 200);
  }

  @Test(
      description = "Fail to accept DRM terms due to already accepted",
      dependsOnMethods = "accept")
  public void alreadyAccepted() throws IOException {
    assertEquals(acceptDrm(ITEM_UUID, 1), 400);
  }

  @Test(description = "Fail to accept DRM terms due to access denied", priority = 10)
  public void accessDenied() throws IOException {
    logout();
    assertEquals(acceptDrm(ITEM_UUID, 1), 403);
  }

  @Test(description = "Successfully list DRM terms")
  public void listTerms() throws IOException {
    JsonNode result = listDrmTerms("677a4bbc-defc-4dc1-b68e-4e2473b66a6a", 1);
    assertNotNull(result.get("terms"));
    assertNotNull(result.get("regularPermission"));
  }

  private int acceptDrm(String uuid, int version) throws IOException {
    final PostMethod method = new PostMethod(DRM_API_ENDPOINT + uuid + "/" + version);
    return makeClientRequest(method);
  }

  private JsonNode listDrmTerms(String uuid, int version) throws IOException {
    final GetMethod method = new GetMethod(DRM_API_ENDPOINT + uuid + "/" + version);
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, 200);

    return mapper.readTree(method.getResponseBody());
  }
}
