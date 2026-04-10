package io.github.openequella.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tle.webtests.framework.TestInstitution;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("rest")
public class LtiPlatformTest extends AbstractRestApiTest {
  final String INSTITUTION_URL = getTestConfig().getInstitutionUrl();
  final String LTI_PLATFORM_API_ENDPOINT = INSTITUTION_URL + "api/ltiplatform";
  final String JWKS_URL = INSTITUTION_URL + ".well-known/jwks.json";
  final String MOODLE_PLATFORM_ID = "http://localhost:8100";
  final String MOODLE_PLATFORM_ID_DOUBLE_ENCODED = "http%253A%252F%252Flocalhost%253A8100";
  final String BRIGHTSPACE_PLATFORM_ID = "http://localhost:8300";
  final String BRIGHTSPACE_PLATFORM_ID_DOUBLE_ENCODED = "http%253A%252F%252Flocalhost%253A8300";
  final String BRIGHTSPACE_URL =
      LTI_PLATFORM_API_ENDPOINT + "/" + BRIGHTSPACE_PLATFORM_ID_DOUBLE_ENCODED;
  final String LTI_ROLE_LEARNER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner";
  final String LTI_ROLE_TEACHER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Teacher";
  final String OEQ_ROLE_LEARNER = "2583c1cb-109d-474a-a76b-d4af3859a467";
  final String OEQ_ROLE_TEACHER = "5553c1cb-109d-474a-a76b-d4af3859a467";
  final String OEQ_ROLE_TUTOR = "4443c1cb-109d-474a-a76b-d4af3859a467";
  final String OEQ_ROLE_BUILDER = "3333c1cb-109d-474a-a76b-d4af3859a467";

  @Test(description = "Retrieve a LTI platform by ID")
  public void getPlatformByID() throws IOException {
    JsonNode results = getPlatform(MOODLE_PLATFORM_ID_DOUBLE_ENCODED);
    assertEquals(MOODLE_PLATFORM_ID, results.get("platformId").asText());
  }

  @Test(description = "Retrieve a list of LTI platforms")
  public void getPlatforms() throws IOException {
    final HttpMethod method = new GetMethod(LTI_PLATFORM_API_ENDPOINT);

    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));
    JsonNode results = mapper.readTree(method.getResponseBodyAsStream());
    assertTrue(results.size() > 0);
  }

  @Test(description = "Create a new LTI platform")
  public void createPlatform() throws IOException {
    final PostMethod method = new PostMethod(LTI_PLATFORM_API_ENDPOINT);

    ObjectNode body = buildRequestBody();
    body.put("platformId", BRIGHTSPACE_PLATFORM_ID);

    int resp_code = makeClientRequestWithEntity(method, body);
    assertEquals(HttpStatus.SC_CREATED, resp_code);

    // The response header should contain the location of the new platform. Also, the platform ID
    // present in the location should be
    // double encoded.
    String location = method.getResponseHeader("Location").getValue();
    assertEquals(
        "http://localhost:8080/rest/api/ltiplatform/http%253A%252F%252Flocalhost%253A8300",
        location);

    // Now use the location to get the platform again.
    JsonNode results = getPlatform(BRIGHTSPACE_PLATFORM_ID_DOUBLE_ENCODED);
    assertTrue(results.size() > 0);
  }

  @Test(description = "Update an existing LTI platform", dependsOnMethods = "createPlatform")
  public void updatePlatform() throws IOException {
    final PutMethod method = new PutMethod(LTI_PLATFORM_API_ENDPOINT);
    ObjectNode body = buildRequestBody();

    // Change some values.
    body.put("platformId", BRIGHTSPACE_PLATFORM_ID);
    body.put("enabled", "false");
    body.set(
        "customRoles",
        mapper
            .createObjectNode()
            .set(LTI_ROLE_LEARNER, mapper.createArrayNode().add(OEQ_ROLE_LEARNER)));

    int resp_code = makeClientRequestWithEntity(method, body);
    assertEquals(HttpStatus.SC_OK, resp_code);

    // Now get the platform again and check if it has been updated.
    JsonNode results = getPlatform(BRIGHTSPACE_PLATFORM_ID_DOUBLE_ENCODED);
    assertFalse(results.get("enabled").asBoolean());
    ArrayNode targets = (ArrayNode) results.get("customRoles").get(LTI_ROLE_LEARNER);
    assertEquals(targets.get(0).asText(), OEQ_ROLE_LEARNER);
  }

  @Test(
      description = "Bulk update enabled status of LTI platform",
      dependsOnMethods = "createPlatform")
  public void updateEnabledStatus() throws IOException {
    // get current enabled status for platform BrightSpace
    Boolean initialEnabledStatus =
        getPlatform(BRIGHTSPACE_PLATFORM_ID_DOUBLE_ENCODED).get("enabled").asBoolean();

    final PutMethod method = new PutMethod(LTI_PLATFORM_API_ENDPOINT + "/enabled");

    Boolean targetEnabledStatus = !initialEnabledStatus;
    ObjectNode brightspacePlatform =
        buildEnabledStatus(BRIGHTSPACE_PLATFORM_ID, targetEnabledStatus);
    ObjectNode unknownPlatform = buildEnabledStatus("unknown", true);

    ArrayNode arrayNode = mapper.createArrayNode();
    arrayNode.add(brightspacePlatform);
    arrayNode.add(unknownPlatform);

    method.setRequestEntity(
        new StringRequestEntity(arrayNode.toString(), "application/json", "UTF-8"));
    int resp_code = makeClientRequest(method);
    assertEquals(207, resp_code);

    JsonNode results = mapper.readTree(method.getResponseBodyAsStream());
    // The first platform should be updated and second one should fail due to not found (404).
    assertEquals(200, results.get(0).get("status").asInt());
    assertEquals(404, results.get(1).get("status").asInt());
    // make sure the enabled value has been changed
    Boolean updatedEnabledStatus =
        getPlatform(BRIGHTSPACE_PLATFORM_ID_DOUBLE_ENCODED).get("enabled").asBoolean();
    assertNotEquals(
        "Platform status should have changed", initialEnabledStatus, updatedEnabledStatus);
    assertEquals(targetEnabledStatus, updatedEnabledStatus);
  }

  @Test(description = "Rotate keypair for an LTI platform", dependsOnMethods = "createPlatform")
  public void rotateKeypair() throws IOException {
    final HttpMethod getOriginalId = new GetMethod(BRIGHTSPACE_URL);
    makeClientRequest(getOriginalId);
    String originalKeyPairId =
        mapper.readTree(getOriginalId.getResponseBodyAsStream()).get("kid").asText();

    final GetMethod getMethod = new GetMethod(BRIGHTSPACE_URL + "/rotated-keys");
    int resp_code = makeClientRequest(getMethod);
    String keyPairId =
        new String(getMethod.getResponseBodyAsStream().readAllBytes(), StandardCharsets.UTF_8);

    final HttpMethod getNewId = new GetMethod(BRIGHTSPACE_URL);
    makeClientRequest(getNewId);
    String newKeyPairId = mapper.readTree(getNewId.getResponseBodyAsStream()).get("kid").asText();

    final HttpMethod getJWKS = new GetMethod(JWKS_URL);
    makeClientRequest(getJWKS);
    JsonNode jwks = mapper.readTree(getJWKS.getResponseBodyAsStream()).get("keys");

    assertEquals(HttpStatus.SC_OK, resp_code);
    // make sure new key is different from previous key pair
    assertNotEquals(newKeyPairId, originalKeyPairId);
    // make sure the created key pair is matched with new activated key pair
    assertEquals(keyPairId, newKeyPairId);
    // make sure new key pair can be found in JWKS
    assertTrue(
        StreamSupport.stream(jwks.spliterator(), false)
            .anyMatch(key -> key.get("kid").asText().equals(keyPairId)));
  }

  @Test(
      description = "Delete an LTI platform",
      dependsOnMethods = {"rotateKeypair", "updateEnabledStatus", "updatePlatform"})
  public void deletePlatform() throws IOException {
    final DeleteMethod method = new DeleteMethod(BRIGHTSPACE_URL);

    int resp_code = makeClientRequest(method);
    assertEquals(HttpStatus.SC_OK, resp_code);

    // Now getting the platform again should return 404.
    assertEquals(HttpStatus.SC_NOT_FOUND, makeClientRequest(new GetMethod(BRIGHTSPACE_URL)));
  }

  @Test(description = "Bulk delete TI platform", dependsOnMethods = "getPlatformByID")
  public void deletePlatforms() throws IOException {
    final DeleteMethod method = new DeleteMethod(LTI_PLATFORM_API_ENDPOINT);

    NameValuePair[] ids =
        new NameValuePair[] {
          new NameValuePair("ids", MOODLE_PLATFORM_ID), new NameValuePair("ids", "unknown platform")
        };
    method.setQueryString(ids);

    int resp_code = makeClientRequest(method);
    assertEquals(207, resp_code);

    JsonNode results = mapper.readTree(method.getResponseBodyAsStream());
    // The first platform should be deleted and second one should result in a 404 error.
    assertEquals(200, results.get(0).get("status").asInt());
    assertEquals(404, results.get(1).get("status").asInt());
  }

  @DataProvider(name = "invalidBeans")
  public Object[][] invalidBeans() {
    ObjectNode invalidUrl = buildRequestBody();
    invalidUrl.put("authUrl", "test");

    ObjectNode emptyClientId = buildRequestBody();
    emptyClientId.put("clientId", "");

    ObjectNode wrongUnknownUserHandling = buildRequestBody();
    wrongUnknownUserHandling.put("unknownUserHandling", "RUN");

    ObjectNode invalidACLExpression = buildRequestBody();
    invalidACLExpression.put("allowExpression", "");

    return new Object[][] {
      {invalidUrl}, {emptyClientId}, {wrongUnknownUserHandling}, {invalidACLExpression}
    };
  }

  @Test(
      description = "Return 400 if trying to create a platform with invalid values",
      dataProvider = "invalidBeans")
  public void invalidValues(ObjectNode body) throws IOException {
    final PostMethod method = new PostMethod(LTI_PLATFORM_API_ENDPOINT);
    int resp_code = makeClientRequestWithEntity(method, body);
    assertEquals(HttpStatus.SC_BAD_REQUEST, resp_code);
  }

  private JsonNode getPlatform(String platformId) throws IOException {
    final HttpMethod method = new GetMethod(LTI_PLATFORM_API_ENDPOINT + "/" + platformId);
    assertEquals(HttpStatus.SC_OK, makeClientRequest(method));

    return mapper.readTree(method.getResponseBodyAsStream());
  }

  private ObjectNode buildRequestBody() {
    ObjectNode body = mapper.createObjectNode();
    body.put("platformId", MOODLE_PLATFORM_ID);
    body.put("name", "Test");
    body.put("clientId", "test");
    body.put("authUrl", "http://test");
    body.put("keysetUrl", "http://test");
    body.put("unknownUserHandling", "ERROR");
    body.put("enabled", "true");
    body.set("instructorRoles", mapper.createArrayNode().add(OEQ_ROLE_TUTOR));
    body.set("unknownRoles", mapper.createArrayNode().add(OEQ_ROLE_BUILDER));
    body.set(
        "customRoles",
        mapper
            .createObjectNode()
            .set(LTI_ROLE_TEACHER, mapper.createArrayNode().add(OEQ_ROLE_TEACHER)));

    return body;
  }

  private ObjectNode buildEnabledStatus(String platformId, Boolean status) {
    ObjectNode platform = mapper.createObjectNode();
    platform.put("platformId", platformId);
    platform.put("enabled", status);
    return platform;
  }
}
