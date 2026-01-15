/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.openequella.rest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class WorkflowTrendsApiTest extends AbstractRestApiTest {
  private final String TARGET_WORKFLOW_UUID = "0f7bd496-8466-4fa5-b166-8132cc5294e4";

  private String getWorkflowTrendsEndpoint() {
    return getTestConfig().getInstitutionUrl() + "api/workflow/trends";
  }

  private String getSpecificWorkflowEndpointTemplate() {
    return getTestConfig().getInstitutionUrl() + "api/workflow/%s/trends";
  }

  // --- Tests for GET all workflow trends---

  @DataProvider(name = "trendValues")
  public Object[][] trendValues() {
    return new Object[][] {{"WEEK"}, {"MONTH"}, {"week"}};
  }

  @Test(
      description = "Retrieve workflow trends with valid trend values",
      dataProvider = "trendValues")
  public void getTrendsWithValidValue(String trendValue) throws IOException {
    JsonNode result = executeRequest(getWorkflowTrendsEndpoint(), trendValue);
    assertNotNull(result);
    validateResponseStructure(result);
  }

  @Test(description = "Fail to retrieve trends with invalid trend value")
  public void getInvalidTrends() throws IOException {
    final HttpMethod method = new GetMethod(getWorkflowTrendsEndpoint());
    method.setQueryString(new NameValuePair[] {new NameValuePair("trend", "YEAR")});

    assertStatusCode(method, HttpStatus.SC_BAD_REQUEST);
  }

  // --- Tests for GET specific workflow trends ---

  @Test(description = "Retrieve trends for a specific workflow")
  public void getSpecificWorkflowTrends() throws IOException {
    String endpoint = String.format(getSpecificWorkflowEndpointTemplate(), TARGET_WORKFLOW_UUID);

    JsonNode result = executeRequest(endpoint, "WEEK");

    assertNotNull(result);
    validateResponseStructure(result);
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid UUID")
  public void getSpecificWorkflowInvalidUuid() throws IOException {
    String endpoint = String.format(getSpecificWorkflowEndpointTemplate(), "invalid-uuid-12345");

    final HttpMethod method = buildGetMethod(endpoint, "WEEK");
    assertStatusCode(method, HttpStatus.SC_NOT_FOUND);
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid trend")
  public void getSpecificWorkflowInvalidTrend() throws IOException {
    String endpoint = String.format(getSpecificWorkflowEndpointTemplate(), TARGET_WORKFLOW_UUID);
    final HttpMethod method = new GetMethod(endpoint);
    method.setQueryString(new NameValuePair[] {new NameValuePair("trend", "INVALID")});

    assertStatusCode(method, HttpStatus.SC_BAD_REQUEST);
  }

  /**
   * Execute a GET request against the workflow trends endpoint with an optional trend parameter,
   * asserting a 200 OK response and returning the parsed JSON.
   */
  private JsonNode executeRequest(String url, String trendParam) throws IOException {
    final HttpMethod method = buildGetMethod(url, trendParam);
    int statusCode = makeClientRequest(method);
    if (statusCode != HttpStatus.SC_OK) {
      throw new IOException("Request failed. Expected 200 OK, but received status: " + statusCode);
    }
    return mapper.readTree(method.getResponseBodyAsStream());
  }

  /** Build a GET method with an optional "trend" query parameter. */
  private HttpMethod buildGetMethod(String url, String trendParam) {
    final HttpMethod method = new GetMethod(url);
    if (trendParam != null) {
      method.setQueryString(new NameValuePair[] {new NameValuePair("trend", trendParam)});
    }
    return method;
  }

  /** Assert that executing the given HTTP method returns the expected status code. */
  private void assertStatusCode(HttpMethod method, int expectedCode) throws IOException {
    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, expectedCode);
  }

  /**
   * Validates that the provided JSON response complies with the expected structure for Workflow
   * Trends.
   *
   * @param result The JsonNode containing the API response body to validate.
   */
  private void validateResponseStructure(JsonNode result) {
    assertTrue(result.isArray(), "Response should be an array");

    for (JsonNode item : result) {
      assertNotNull(item.get("taskId"), "taskId field should exist");
      assertTrue(item.get("taskId").isTextual(), "taskId should be a string");

      assertNotNull(item.get("name"), "name field should exist");
      assertTrue(item.get("name").isTextual(), "name should be a string");

      assertNotNull(item.get("waiting"), "waiting field should exist");
      assertTrue(item.get("waiting").isInt(), "waiting should be an integer");

      assertNotNull(item.get("trend"), "trend field should exist");
      assertTrue(item.get("trend").isInt(), "trend should be an integer");
    }
  }
}
