package io.github.openequella.rest

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.{DeleteMethod, GetMethod, PutMethod}
import org.junit.Assert.assertEquals
import org.testng.annotations.Test

class DashboardApiTest extends AbstractRestApiTest {
  private val DASHBOARD_API_ENDPOINT     = getTestConfig.getInstitutionUrl + "api/dashboard"
  private val DASHBOARD_LAYOUT_ENDPOINT  = DASHBOARD_API_ENDPOINT + "/layout"
  private val DASHBOARD_PORTLET_ENDPOINT = DASHBOARD_API_ENDPOINT + "/portlet"
  private val LAYOUT                     = "layout"
  private val ADMIN_PORTLET_UUID         = "ddd84757-8319-4816-b0e0-39c71a0ba691"

  @Test(description = "Retrieve dashboard details")
  def dashboardDetails(): Unit = {
    val request = new GetMethod(DASHBOARD_API_ENDPOINT)

    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, respCode)

    val details = getDashboardDetails
    assertEquals(
      3,
      details.get("portlets").size()
    ) // User 'autotest' has 4 portlets but 1 is closed.
    assertEquals("SingleColumn", details.get(LAYOUT).asText)
  }

  @Test(description = "Retrieve a list portlet types that the user can create")
  def creatablePortlet(): Unit = {
    val request = new GetMethod(s"$DASHBOARD_PORTLET_ENDPOINT/creatable")

    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, respCode)

    val resp = mapper.readTree(request.getResponseBodyAsStream)
    // There are 11 portlet types defined but two are deprecated so result should contain 9 types.
    assertEquals(9, resp.size())
  }

  @Test(description = "Retrieve a list of closed portlets")
  def closedPortlet(): Unit = {
    val request = new GetMethod(s"$DASHBOARD_PORTLET_ENDPOINT/closed")

    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, respCode)

    val resp = mapper.readTree(request.getResponseBodyAsStream)
    assertEquals(1, resp.size())

    val closedPortlet = resp.get(0)
    assertEquals("Admin search", closedPortlet.get("name").asText)
    assertEquals("ddd84757-8319-4816-b0e0-39c71a0ba691", closedPortlet.get("uuid").asText)
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

  @Test(
    description = "Update portlet preference"
  )
  def preferenceUpdate(): Unit = {
    val recentContributionUuid = "6e34ab70-a8b2-4e7b-84b9-4dcff91470b7"

    // The Recent contribution portlet is returned as the first portlet in the list.
    def getRecentContributionPortlet = getPortletDetails.get(0).get("commonDetails")

    val portlet = getRecentContributionPortlet
    // Verify the initial state of the Recent contribution portlet.
    assertEquals(recentContributionUuid, portlet.get("uuid").asText)
    assertEquals(false, portlet.get("isClosed").asBoolean)
    assertEquals(false, portlet.get("isMinimised").asBoolean)
    assertEquals(0, portlet.get("column").asInt)
    assertEquals(0, portlet.get("order").asInt)

    // Update the portlet preference now.
    val updateRequest = new PutMethod(
      s"$DASHBOARD_API_ENDPOINT/portlet/$recentContributionUuid/preference"
    )
    val preferenceUpdate = mapper.createObjectNode
    preferenceUpdate.put("isMinimised", true)
    preferenceUpdate.put("column", 1)
    preferenceUpdate.put("order", 2)

    val updateResultCode = makeClientRequestWithEntity(updateRequest, preferenceUpdate)
    assertEquals(HttpStatus.SC_NO_CONTENT, updateResultCode)

    // Get the portlet details again to verify the preference update.
    val updatedPortlet = getRecentContributionPortlet
    assertEquals(true, updatedPortlet.get("isMinimised").asBoolean)
    assertEquals(1, updatedPortlet.get("column").asInt)
    assertEquals(2, updatedPortlet.get("order").asInt)
  }

  @Test(description = "Update should fail with a unknown portlet UUID")
  def preferenceUpdateFailed(): Unit = {
    val updateRequest    = new PutMethod(s"$DASHBOARD_API_ENDPOINT/portlet/unknown/preference")
    val updateResultCode = makeClientRequestWithEntity(updateRequest, mapper.createObjectNode)
    assertEquals(HttpStatus.SC_NOT_FOUND, updateResultCode)
  }

  @Test(description = "Delete a private portlet", dependsOnMethods = Array("dashboardDetails"))
  def deletePortlet(): Unit = {
    val deleteRequest = new DeleteMethod(
      s"$DASHBOARD_API_ENDPOINT/portlet/6e34ab70-a8b2-4e7b-84b9-4dcff91470b7"
    )
    val statusCode = makeClientRequestWithEntity(deleteRequest, mapper.createObjectNode)
    assertEquals(HttpStatus.SC_NO_CONTENT, statusCode)
  }

  @Test(description = "Delete a portlet belonging to another user")
  def deleteOtherUserPortlet(): Unit = {
    val deleteRequest = new DeleteMethod(s"$DASHBOARD_API_ENDPOINT/portlet/$ADMIN_PORTLET_UUID")
    val statusCode    = makeClientRequestWithEntity(deleteRequest, mapper.createObjectNode)
    assertEquals(HttpStatus.SC_FORBIDDEN, statusCode)
  }

  @Test(description = "Delete a portlet by a unknown UUID")
  def deleteNonExistingPortlet(): Unit = {
    val deleteRequest = new DeleteMethod(s"$DASHBOARD_API_ENDPOINT/portlet/unknown")
    val statusCode    = makeClientRequestWithEntity(deleteRequest, mapper.createObjectNode)
    assertEquals(HttpStatus.SC_NOT_FOUND, statusCode)
  }

  private def getDashboardDetails: JsonNode = {
    val request  = new GetMethod(DASHBOARD_API_ENDPOINT)
    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, respCode)

    mapper.readTree(request.getResponseBodyAsStream)
  }

  private def getPortletDetails: JsonNode = {
    getDashboardDetails.get("portlets")
  }

}
