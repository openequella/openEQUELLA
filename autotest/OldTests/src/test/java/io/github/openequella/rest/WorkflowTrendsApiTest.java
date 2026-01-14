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
import org.testng.annotations.Test;

public class WorkflowTrendsApiTest extends AbstractRestApiTest {
  private final String WORKFLOW_TRENDS_ENDPOINT =
      getTestConfig().getInstitutionUrl() + "api/workflow/trends";
  private final String SPECIFIC_WORKFLOW_ENDPOINT_TEMPLATE =
      getTestConfig().getInstitutionUrl() + "api/workflow/%s/trends";

  private final String TARGET_WORKFLOW_UUID = "0f7bd496-8466-4fa5-b166-8132cc5294e4";

  // --- Tests for GET all workflow trends---

  @Test(description = "Retrieve workflow trends with 'WEEK' trend")
  public void getWeeklyTrends() throws IOException {
    JsonNode result = doRequest(WORKFLOW_TRENDS_ENDPOINT, HttpStatus.SC_OK, "WEEK");
    assertNotNull(result);
    validateResponseStructure(result);
  }

  @Test(description = "Retrieve workflow trends with 'MONTH' trend")
  public void getMonthlyTrends() throws IOException {
    JsonNode result = doRequest(WORKFLOW_TRENDS_ENDPOINT, HttpStatus.SC_OK, "MONTH");
    assertNotNull(result);
    validateResponseStructure(result);
  }

  @Test(description = "Retrieve workflow trends case insensitive (lowercase 'week')")
  public void getWeeklyTrendsLowerCase() throws IOException {
    JsonNode result = doRequest(WORKFLOW_TRENDS_ENDPOINT, HttpStatus.SC_OK, "week");
    assertNotNull(result);
    validateResponseStructure(result);
  }

  @Test(description = "Fail to retrieve trends with invalid trend value")
  public void getInvalidTrends() throws IOException {
    final HttpMethod method = new GetMethod(WORKFLOW_TRENDS_ENDPOINT);
    method.setQueryString(new NameValuePair[] {new NameValuePair("trend", "YEAR")});

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, HttpStatus.SC_BAD_REQUEST);
  }

  // --- Tests for GET specific workflow trends ---

  @Test(description = "Retrieve trends for a specific workflow")
  public void getSpecificWorkflowTrends() throws IOException {
    String endpoint = String.format(SPECIFIC_WORKFLOW_ENDPOINT_TEMPLATE, TARGET_WORKFLOW_UUID);

    JsonNode result = doRequest(endpoint, HttpStatus.SC_OK, "WEEK");

    assertNotNull(result);
    validateResponseStructure(result);
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid UUID")
  public void getSpecificWorkflowInvalidUuid() throws IOException {
    String endpoint = String.format(SPECIFIC_WORKFLOW_ENDPOINT_TEMPLATE, "invalid-uuid-12345");

    doRequest(endpoint, HttpStatus.SC_NOT_FOUND, "WEEK");
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid trend")
  public void getSpecificWorkflowInvalidTrend() throws IOException {
    String endpoint = String.format(SPECIFIC_WORKFLOW_ENDPOINT_TEMPLATE, TARGET_WORKFLOW_UUID);
    final HttpMethod method = new GetMethod(endpoint);
    method.setQueryString(new NameValuePair[] {new NameValuePair("trend", "INVALID")});

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, HttpStatus.SC_BAD_REQUEST);
  }

  private JsonNode doRequest(String url, int expectedCode, String trendParam) throws IOException {
    final HttpMethod method = new GetMethod(url);
    if (trendParam != null) {
      method.setQueryString(new NameValuePair[] {new NameValuePair("trend", trendParam)});
    }

    int statusCode = makeClientRequest(method);
    assertEquals(statusCode, expectedCode);

    if (expectedCode == HttpStatus.SC_OK) {
      return mapper.readTree(method.getResponseBodyAsStream());
    }
    return null;
  }

  private void validateResponseStructure(JsonNode result) {
    assertTrue(result.isArray());

    if (!result.isEmpty()) {
      JsonNode item = result.get(0);

      assertNotNull(item.get("taskId"));
      assertNotNull(item.get("name"));
      assertNotNull(item.get("waiting"));
      assertNotNull(item.get("trend"));
    }
  }
}
