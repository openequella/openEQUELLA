package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, NameValuePair}
import org.testng.Assert.{assertEquals, assertNotNull, assertTrue}
import org.testng.annotations.{DataProvider, Test}

import java.io.IOException

class WorkflowTrendsApiTest extends AbstractRestApiTest {

  private val TargetWorkflowUuid       = "0f7bd496-8466-4fa5-b166-8132cc5294e4"
  private val NoTasksWorkflowUuid      = "23ced460-f8f8-4bd4-bdc2-971064636de8"
  private val NoPermissionWorkflowUuid = "117dead8-767d-4eff-ba06-5e1964f2f2db"

  private val TrendWeek  = "WEEK"
  private val TrendMonth = "MONTH"

  private def getWorkflowTrendsEndpoint: String =
    getTestConfig.getInstitutionUrl + "api/workflow/trends"

  private def getSpecificWorkflowEndpointTemplate: String =
    getTestConfig.getInstitutionUrl + "api/workflow/%s/trends"

  // --- Tests for GET all workflow trends ---

  @DataProvider(name = "trendValues")
  def trendValues: Array[Array[Object]] = Array(
    Array(TrendWeek),
    Array(TrendMonth),
    Array(TrendWeek.toLowerCase)
  )

  @Test(
    description = "Retrieve workflow trends with valid trend values",
    dataProvider = "trendValues"
  )
  def trendsWithValidValue(trendValue: String): Unit = {
    val result = executeRequest(getWorkflowTrendsEndpoint, trendValue)
    assertNotNull(result)
    validateResponseStructure(result)
  }

  @DataProvider(name = "invalidTrendValues")
  def invalidTrendValues: Array[Array[Object]] = Array(
    Array("YEAR"),
    Array(""),
    Array(null)
  )

  @Test(
    description = "Fail to retrieve workflow trends with invalid, empty, or missing trend values",
    dataProvider = "invalidTrendValues"
  )
  def trendsWithInvalidValue(trendValue: String): Unit = {
    val method = buildGetMethod(getWorkflowTrendsEndpoint, trendValue)
    assertStatusCode(method, HttpStatus.SC_BAD_REQUEST)
  }

  @Test(description = "Verify empty response structure when user lacks manage workflow permission")
  def emptyTrendsResponse(): Unit = {
    loginAsLowPrivilegeUser()
    val result = executeRequest(getWorkflowTrendsEndpoint, TrendWeek)
    assertTrue(result.isArray, "Response should be a JSON array")
    assertEquals(result.size(), 0, "Expected empty array for user with no workflow permissions")
    login()
  }

  // --- Tests for GET specific workflow trends ---

  @Test(description = "Retrieve trends for a specific workflow")
  def specificWorkflowTrends(): Unit = {
    val endpoint = getSpecificWorkflowEndpointTemplate.format(TargetWorkflowUuid)
    val result   = executeRequest(endpoint, TrendWeek)

    assertNotNull(result)
    validateResponseStructure(result)
  }

  @Test(description = "Retrieve an empty list if the workflow has no waiting tasks")
  def specificWorkflowNoTasks(): Unit = {
    val endpoint = getSpecificWorkflowEndpointTemplate.format(NoTasksWorkflowUuid)
    val result   = executeRequest(endpoint, TrendWeek)

    assertTrue(result.isArray, "Response should be a JSON array")
    assertEquals(result.size(), 0, "Response array should be empty")
  }

  @Test(description =
    "Verify 403 Forbidden when user lacks MANAGE_WORKFLOW permission for specific workflow"
  )
  def trendsAccessDenied(): Unit = {
    val endpoint = getSpecificWorkflowEndpointTemplate.format(NoPermissionWorkflowUuid)
    val method   = buildGetMethod(endpoint, TrendWeek)

    assertStatusCode(method, HttpStatus.SC_FORBIDDEN)
  }

  @Test(description = "Reject null UUID parameter")
  def nullUuidParameter(): Unit = {
    val endpoint = getSpecificWorkflowEndpointTemplate.format(null)
    val method   = buildGetMethod(endpoint, TrendWeek)

    assertStatusCode(method, HttpStatus.SC_BAD_REQUEST)
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid UUID")
  def specificWorkflowInvalidUuid(): Unit = {
    val endpoint =
      getSpecificWorkflowEndpointTemplate.format("0f7bd496-8466-4fa5-b166-8832cc5294e4")

    val method = buildGetMethod(endpoint, TrendWeek)
    assertStatusCode(method, HttpStatus.SC_NOT_FOUND)
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid trend")
  def specificWorkflowInvalidTrend(): Unit = {
    val endpoint = getSpecificWorkflowEndpointTemplate.format(TargetWorkflowUuid)
    val method   = buildGetMethod(endpoint, "INVALID")

    assertStatusCode(method, HttpStatus.SC_BAD_REQUEST)
  }

  /** Execute a GET request against the workflow trends endpoint with an optional trend parameter,
    * asserting a 200 OK response and returning the parsed JSON.
    */
  private def executeRequest(url: String, trendParam: String): JsonNode = {
    val method     = buildGetMethod(url, trendParam)
    val statusCode = makeClientRequest(method)
    if (statusCode != HttpStatus.SC_OK) {
      throw new IOException(s"Request failed. Expected 200 OK, but received status: $statusCode")
    }
    mapper.readTree(method.getResponseBodyAsStream)
  }

  /** Build a GET method with an optional "trend" query parameter. */
  private def buildGetMethod(url: String, trendParam: String): HttpMethod = {
    val method = new GetMethod(url)
    if (trendParam != null) {
      method.setQueryString(Array(new NameValuePair("trend", trendParam)))
    }
    method
  }

  /** Assert that executing the given HTTP method returns the expected status code. */
  private def assertStatusCode(method: HttpMethod, expectedCode: Int): Unit = {
    val statusCode = makeClientRequest(method)
    assertEquals(statusCode, expectedCode)
  }

  /** Validates that the provided JSON response complies with the expected structure for Workflow
    * Trends.
    *
    * @param result
    *   The JsonNode containing the API response body to validate.
    */
  private def validateResponseStructure(result: JsonNode): Unit = {
    assertTrue(result.isArray, "Response should be an array")

    val elements = result.elements()
    while (elements.hasNext) {
      val item = elements.next()
      assertNotNull(item.get("taskId"), "taskId field should exist")
      assertTrue(item.get("taskId").isTextual, "taskId should be a string")

      assertNotNull(item.get("name"), "name field should exist")
      assertTrue(item.get("name").isTextual, "name should be a string")

      assertNotNull(item.get("waiting"), "waiting field should exist")
      assertTrue(item.get("waiting").isInt, "waiting should be an integer")

      assertNotNull(item.get("trend"), "trend field should exist")
      assertTrue(item.get("trend").isInt, "trend should be an integer")
    }
  }
}
