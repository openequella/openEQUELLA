package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, NameValuePair}
import org.testng.Assert.{assertEquals, assertNotNull, assertTrue}
import org.testng.annotations.{DataProvider, Test}

class WorkflowTrendsApiTest extends AbstractRestApiTest {

  private val TargetWorkflowUuid       = "0f7bd496-8466-4fa5-b166-8132cc5294e4"
  private val NoTasksWorkflowUuid      = "23ced460-f8f8-4bd4-bdc2-971064636de8"
  private val NoPermissionWorkflowUuid = "117dead8-767d-4eff-ba06-5e1964f2f2db"

  private val TrendWeek  = "WEEK"
  private val TrendMonth = "MONTH"

  private def getWorkflowTrendsEndpoint: String =
    getTestConfig.getInstitutionUrl + "api/workflow/trends"

  private def getSpecificWorkflowEndpoint(workflowUuid: String): String =
    s"${getTestConfig.getInstitutionUrl}api/workflow/$workflowUuid/trends"

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
    val result = executeRequestExpecting200(getWorkflowTrendsEndpoint, trendValue)
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
  def trendsWithInvalidValue(trendValue: String): Unit =
    executeRequestExpectingStatus(getWorkflowTrendsEndpoint, trendValue, HttpStatus.SC_BAD_REQUEST)

  @Test(description = "Verify empty response structure when user lacks manage workflow permission")
  def emptyTrendsResponse(): Unit = {
    loginAsLowPrivilegeUser()
    val result = executeRequestExpecting200(getWorkflowTrendsEndpoint, TrendWeek)
    assertTrue(result.isArray, "Response should be a JSON array")
    assertEquals(result.size(), 0, "Expected empty array for user with no workflow permissions")
    login()
  }

  // --- Tests for GET specific workflow trends ---

  @Test(description = "Retrieve trends for a specific workflow")
  def specificWorkflowTrends(): Unit = {
    val endpoint = getSpecificWorkflowEndpoint(TargetWorkflowUuid)
    val result   = executeRequestExpecting200(endpoint, TrendWeek)

    assertNotNull(result)
    validateResponseStructure(result)
  }

  @Test(description = "Retrieve an empty list if the workflow has no waiting tasks")
  def specificWorkflowNoTasks(): Unit = {
    val endpoint = getSpecificWorkflowEndpoint(NoTasksWorkflowUuid)
    val result   = executeRequestExpecting200(endpoint, TrendWeek)

    assertTrue(result.isArray, "Response should be a JSON array")
    assertTrue(result.isEmpty, "Response array should be empty")
  }

  @Test(description =
    "Verify 403 Forbidden when user lacks MANAGE_WORKFLOW permission for specific workflow"
  )
  def trendsAccessDenied(): Unit = {
    val endpoint = getSpecificWorkflowEndpoint(NoPermissionWorkflowUuid)
    executeRequestExpectingStatus(endpoint, TrendWeek, HttpStatus.SC_FORBIDDEN)
  }

  @Test(description = "Reject null UUID parameter")
  def nullUuidParameter(): Unit = {
    val endpoint = getSpecificWorkflowEndpoint(null)
    executeRequestExpectingStatus(endpoint, TrendWeek, HttpStatus.SC_BAD_REQUEST)
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid UUID")
  def specificWorkflowInvalidUuid(): Unit = {
    val endpoint =
      getSpecificWorkflowEndpoint("0f7bd496-8466-4fa5-b166-8832cc5294e4")

    executeRequestExpectingStatus(endpoint, TrendWeek, HttpStatus.SC_NOT_FOUND)
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid trend")
  def specificWorkflowInvalidTrend(): Unit = {
    val endpoint = getSpecificWorkflowEndpoint(TargetWorkflowUuid)
    executeRequestExpectingStatus(endpoint, "INVALID", HttpStatus.SC_BAD_REQUEST)
  }

  /** Execute a GET request and return the parsed JSON, explicitly asserting a 200 OK status.
    */
  private def executeRequestExpecting200(url: String, trendParam: String): JsonNode = {
    val method     = buildGetMethod(url, trendParam)
    val statusCode = makeClientRequest(method)
    assertEquals(
      statusCode,
      HttpStatus.SC_OK,
      s"Request failed. Expected 200 OK, but received $statusCode"
    )

    mapper.readTree(method.getResponseBodyAsStream)
  }

  /** Execute a GET request and assert that the returned status code matches the expected code. This
    * unifies the testing style for negative test cases.
    */
  private def executeRequestExpectingStatus(
      url: String,
      trendParam: String,
      expectedCode: Int
  ): Unit = {
    val method     = buildGetMethod(url, trendParam)
    val statusCode = makeClientRequest(method)
    assertEquals(
      statusCode,
      expectedCode,
      s"Expected status code $expectedCode but received $statusCode"
    )
  }

  /** Build a GET method with an optional "trend" query parameter. */
  private def buildGetMethod(url: String, trendParam: String): HttpMethod = {
    val method = new GetMethod(url)
    if (trendParam != null) {
      method.setQueryString(Array(new NameValuePair("trend", trendParam)))
    }
    method
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
