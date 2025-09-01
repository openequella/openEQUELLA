package io.github.openequella.rest

import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.{GetMethod, PutMethod}
import org.junit.Assert.assertEquals
import org.testng.annotations.Test

class DashboardApiTest extends AbstractRestApiTest {
  private val DASHBOARD_API_ENDPOINT    = getTestConfig.getInstitutionUrl + "api/dashboard"
  private val DASHBOARD_LAYOUT_ENDPOINT = DASHBOARD_API_ENDPOINT + "/layout"
  private val LAYOUT                    = "layout"

  @Test(description = "Retrieve dashboard details")
  def dashboardDetails(): Unit = {
    val request = new GetMethod(DASHBOARD_API_ENDPOINT)

    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, respCode)

    val resp = mapper.readTree(request.getResponseBodyAsStream)
    assertEquals(4, resp.get("portlets").size()) // User 'autotest' has 4 viewable portlets.
    assertEquals("SingleColumn", resp.get(LAYOUT).asText)
  }

  @Test(
    description = "Update dashboard layout using a valid value",
    dependsOnMethods = Array("dashboardDetails")
  )
  def dashboardLayoutUpdate(): Unit = {
    val newLayout = "TwoEqualColumns"

    val updateRequest = new PutMethod(DASHBOARD_LAYOUT_ENDPOINT)
    val body          = mapper.createObjectNode
    body.put(LAYOUT, newLayout)
    val updateResultCode = makeClientRequestWithEntity(updateRequest, body)
    assertEquals(HttpStatus.SC_NO_CONTENT, updateResultCode)

    // Get the dashboard details to verify the layout was updated.
    val request = new GetMethod(DASHBOARD_API_ENDPOINT)
    makeClientRequest(request)
    val resp = mapper.readTree(request.getResponseBodyAsStream)
    assertEquals(newLayout, resp.get(LAYOUT).asText)
  }

  @Test(
    description = "Update dashboard layout using an invalid value"
  )
  def dashboardLayoutUpdateFailed(): Unit = {
    val updateRequest = new PutMethod(DASHBOARD_LAYOUT_ENDPOINT)
    val body          = mapper.createObjectNode
    body.put(LAYOUT, "ThreeColumns")
    val updateResultCode = makeClientRequestWithEntity(updateRequest, body)
    assertEquals(HttpStatus.SC_BAD_REQUEST, updateResultCode)
  }
}
