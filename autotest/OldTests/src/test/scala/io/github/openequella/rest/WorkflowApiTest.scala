package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.httpclient.methods.GetMethod
import org.apache.commons.httpclient.{HttpMethod, HttpStatus, NameValuePair}
import org.testng.Assert.{assertEquals, assertNotNull, assertTrue}
import org.testng.annotations.{DataProvider, Test}

class WorkflowApiTest extends AbstractRestApiTest {
  private val TargetWorkflowUuid       = "0f7bd496-8466-4fa5-b166-8132cc5294e4"
  private val NoTasksWorkflowUuid      = "23ced460-f8f8-4bd4-bdc2-971064636de8"
  private val NoPermissionWorkflowUuid = "117dead8-767d-4eff-ba06-5e1964f2f2db"

  private val TrendWeek  = "WEEK"
  private val TrendMonth = "MONTH"

  private val BASE_API_PATH = s"${getTestConfig.getInstitutionUrl}api/workflow"

  private val workflowTrendsEndpoint = s"${BASE_API_PATH}/trends"

  private def getWorkflowStatisticsEndpoint(workflowUuid: String): String =
    s"${BASE_API_PATH}/$workflowUuid/statistics"

  private val getManageableWorkflowsEndpoint: String =
    s"${BASE_API_PATH}/manageable"

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
    val result = executeRequestExpecting200(workflowTrendsEndpoint, Option(trendValue))
    assertNotNull(result)
    validateTrendsStructure(result)
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
    executeRequestExpectingStatus(workflowTrendsEndpoint, trendValue, HttpStatus.SC_BAD_REQUEST)

  @Test(description = "Verify empty response structure when user lacks manage workflow permission")
  def emptyTrendsResponse(): Unit = {
    loginAsLowPrivilegeUser()
    val result = executeRequestExpecting200(workflowTrendsEndpoint, Option(TrendWeek))
    assertTrue(result.isArray, "Response should be a JSON array")
    assertEquals(result.size(), 0, "Expected empty array for user with no workflow permissions")
    login()
  }

  // --- Tests for GET workflow statistics ---

  @Test(description = "Retrieve trends and item count for a specific workflow")
  def workflowStatistics(): Unit = {
    val endpoint = getWorkflowStatisticsEndpoint(TargetWorkflowUuid)
    val result   = executeRequestExpecting200(endpoint, Option(TrendWeek))

    assertNotNull(result)
    validateStatisticsStructure(result)
  }

  @Test(description = "Retrieve an empty list if the workflow has no waiting tasks")
  def workflowStatisticsNoTasks(): Unit = {
    val endpoint = getWorkflowStatisticsEndpoint(NoTasksWorkflowUuid)
    val result   = executeRequestExpecting200(endpoint, Option(TrendWeek))

    val taskTrends = getTaskTrends(result)
    val itemCount  = getItemCount(result)

    assertTrue(taskTrends.isArray, "Task trends should be a JSON array")
    assertTrue(taskTrends.isEmpty, "Task trends array should be empty")
    assertEquals(itemCount.asInt(), 0, "Item count should be zero")
  }

  @Test(description =
    "Verify 403 Forbidden when user lacks MANAGE_WORKFLOW permission for specific workflow"
  )
  def workflowStatisticsAccessDenied(): Unit = {
    val endpoint = getWorkflowStatisticsEndpoint(NoPermissionWorkflowUuid)
    executeRequestExpectingStatus(endpoint, TrendWeek, HttpStatus.SC_FORBIDDEN)
  }

  @Test(description = "Reject null UUID parameter")
  def nullUuidParameter(): Unit = {
    val endpoint = getWorkflowStatisticsEndpoint(null)
    executeRequestExpectingStatus(endpoint, TrendWeek, HttpStatus.SC_BAD_REQUEST)
  }

  @Test(description = "Fail to retrieve specific workflow trends with invalid UUID")
  def workflowStatisticsInvalidUuid(): Unit = {
    val endpoint =
      getWorkflowStatisticsEndpoint("0f7bd496-8466-4fa5-b166-8832cc5294e4")

    executeRequestExpectingStatus(endpoint, TrendWeek, HttpStatus.SC_NOT_FOUND)
  }

  @Test(description = "Fail to retrieve specific workflow statistics with invalid trend")
  def workflowStatisticsInvalidTrend(): Unit = {
    val endpoint = getWorkflowStatisticsEndpoint(TargetWorkflowUuid)
    executeRequestExpectingStatus(endpoint, "INVALID", HttpStatus.SC_BAD_REQUEST)
  }

  // --- Tests for GET manageable workflows ---

  @Test(description = "Get all manageable workflows")
  def manageableWorkflows(): Unit = {
    val result = executeRequestExpecting200(getManageableWorkflowsEndpoint)
    validWorkflowSummary(result)
  }

  @Test(description = "Return empty list if user doesn't have MANAGE_WORKFLOW permission")
  def manageableWorkflowsWithoutPermission(): Unit = {
    loginAsLowPrivilegeUser()

    val result = executeRequestExpecting200(getManageableWorkflowsEndpoint)
    assertTrue(result.isEmpty)

    login()
  }

  /** Execute a GET request and return the parsed JSON, explicitly asserting a 200 OK status.
    */
  private def executeRequestExpecting200(
      url: String,
      trendParam: Option[String] = None
  ): JsonNode = {
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
    val method     = buildGetMethod(url, Option(trendParam))
    val statusCode = makeClientRequest(method)
    assertEquals(
      statusCode,
      expectedCode,
      s"Expected status code $expectedCode but received $statusCode"
    )
  }

  /** Build a GET method with an optional "trend" query parameter. */
  private def buildGetMethod(url: String, trendParam: Option[String]): HttpMethod = {
    val method = new GetMethod(url)
    trendParam.foreach(trend => method.setQueryString(Array(new NameValuePair("trend", trend))))
    method
  }

  private def getItemCount(result: JsonNode): JsonNode = result.get("itemCount")

  private def getTaskTrends(result: JsonNode): JsonNode = result.get("taskTrends")

  private def validWorkflowSummary(result: JsonNode): Unit = {
    assertTrue(result.isArray, "Response should be an array")

    val elements = result.elements()
    while (elements.hasNext) {
      val item = elements.next()
      val uuid = item.get("uuid")
      val name = item.get("name")

      assertNotNull(uuid, "uuid field should exist")
      assertTrue(uuid.isTextual, "uuid should be a string")

      assertNotNull(name, "name field should exist")
      assertTrue(name.isTextual, "name should be a string")
    }
  }

  /** Validates that the provided JSON response complies with the expected structure for Workflow
    * Trends.
    *
    * @param result
    *   The JsonNode containing the API response body to validate.
    */
  private def validateTrendsStructure(result: JsonNode): Unit = {
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

  /** Validates the JSON object structure for workflow statistics: { taskTrends: [...], itemCount:
    * int }.
    */
  private def validateStatisticsStructure(result: JsonNode): Unit = {
    assertTrue(result.isObject, "Response should be an object")

    val itemCount = getItemCount(result)
    assertNotNull(itemCount, "itemCount field should exist")
    assertTrue(itemCount.isInt, "itemCount should be an integer")

    val taskTrends = getTaskTrends(result)
    assertNotNull(taskTrends, "taskTrends field should exist")
    assertTrue(taskTrends.isArray, "taskTrends should be an array")

    validateTrendsStructure(taskTrends)
  }
}
