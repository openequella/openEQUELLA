package io.github.openequella.rest

import com.tle.webtests.framework.TestInstitution
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.GetMethod
import org.junit.Assert.assertEquals
import org.testng.annotations.Test

@TestInstitution("fiveo")
class DashboardApiTest extends AbstractRestApiTest {
  private val DASHBOARD_API_ENDPOINT = getTestConfig.getInstitutionUrl + "api/dashboard"

  @Test(description = "")
  def dashboardDetails(): Unit = {
    val request = new GetMethod(DASHBOARD_API_ENDPOINT)

    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, respCode)

    val resp = mapper.readTree(request.getResponseBodyAsStream)
    assertEquals(4, resp.get("portlets").size())
  }
}
