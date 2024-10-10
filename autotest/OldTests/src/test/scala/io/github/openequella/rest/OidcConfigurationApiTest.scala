package io.github.openequella.rest

import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.methods.{GetMethod, PutMethod}
import org.junit.Assert.assertEquals
import org.testng.annotations.Test

class OidcConfigurationApiTest extends AbstractRestApiTest {
  private val OIDC_ENDPOINT           = getTestConfig.getInstitutionUrl + "api/oidc/config"
  private val PLATFORM                = "GENERIC"
  private val AUTH_URL                = "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/authorize"
  private val AUTH_CODE_CLIENT_SECRET = "authCodeClientSecret"
  private val API_CLIENT_ID           = "1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps"
  private val API_CLIENT_SECRET       = "apiClientSecret"

  private def buildRequestBody = {
    val body = mapper.createObjectNode()
    body.put("name", "Auth0")
    body.put("platform", PLATFORM)
    body.put("authCodeClientId", "C5tvBaB7svqjLPe0dDPBicgPcVPDJumZ")
    body.put(AUTH_CODE_CLIENT_SECRET,
             "_If_ItaRIw6eq0mKGMgoetTLjnGiuGvYbC012yA26F8I4vIZ7PaLGYwF3T89Yo1L")
    body.put("authUrl", AUTH_URL)
    body.put("keysetUrl", "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/.well-known/jwks.json")
    body.put("tokenUrl", "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/oauth/token")
    body.put("enabled", true)
    body.put("apiUrl", "https://dev-cqchwn4hfdb1p8xr.au.auth0.com/api/v2/users")
    body.put("apiClientId", "1GONnE1LtQ1dU0UU8WK0GR3SpCG8KOps")
    body.put(API_CLIENT_SECRET, "JKpZOuwluzwHnNXR-rxhhq_p4dWmMz-EhtRHjyfza5nCiG-J2SHrdeXAkyv2GB4I")

    val defaultRoles = body.putArray("defaultRoles")
    defaultRoles.add("admin")

    body
  }

  private def getIdentityProvider = {
    val request = new GetMethod(OIDC_ENDPOINT)

    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_OK, respCode)

    mapper.readTree(request.getResponseBody())
  }

  @Test(description = "Create a new OIDC configuration")
  def add(): Unit = {
    val request  = new PutMethod(OIDC_ENDPOINT)
    val respCode = makeClientRequestWithEntity(request, buildRequestBody)
    assertEquals(HttpStatus.SC_OK, respCode)
  }

  @Test(description = "Retrieve the OIDC configuration", dependsOnMethods = Array("add"))
  def get(): Unit = {
    val idp           = getIdentityProvider
    val commonDetails = idp.get("commonDetails")

    // Confirm the common values are returned.
    assertEquals(commonDetails.get("platform").asText(), PLATFORM)
    assertEquals(commonDetails.get("authUrl").asText(), AUTH_URL)
    // Confirm sensitive values are not returned.
    assertEquals(commonDetails.get("authCodeClientSecret"), null)
    // Confirm Platform-specific values are returned.
    assertEquals(idp.get("apiClientId").asText(), API_CLIENT_ID)
  }

  @Test(description = "Update OIDC configuration without providing sensitive values",
        dependsOnMethods = Array("add"))
  def addWithoutSensitiveValues(): Unit = {
    val newName = "Auth0-Updated"

    val body = buildRequestBody
    body.put("name", newName)
    body.remove(AUTH_CODE_CLIENT_SECRET)
    body.remove(API_CLIENT_SECRET)

    val request  = new PutMethod(OIDC_ENDPOINT)
    val respCode = makeClientRequestWithEntity(request, body)
    assertEquals(HttpStatus.SC_OK, respCode)

    // Get again and confirm the values have been returned.
    val idp = getIdentityProvider
    assertEquals(idp.get("commonDetails").get("name").asText(), newName)
  }

  @Test(description = "Return 400 when creating with invalid values")
  def invalidValues(): Unit = {
    val body = buildRequestBody
    body.put("authCodeClientId", "")
    body.put("keysetUrl", "http://abc/ keyset/")
    body.put("apiClientId", "")

    val request  = new PutMethod(OIDC_ENDPOINT)
    val respCode = makeClientRequestWithEntity(request, body)
    assertEquals(HttpStatus.SC_BAD_REQUEST, respCode)

    val result = mapper.readTree(request.getResponseBody())
    val errors = result.get("errors").findValue("message").asText()
    assertEquals(
      "Missing value for required field: Authorisation Code flow Client ID," +
        "Invalid value for Key set URL: Illegal character in path at index 11: http://abc/ keyset/," +
        "Missing value for required field: IdP API Client ID",
      errors
    )
  }

  @Test(description = "Return 400 when creating with unsupported platform")
  def unsupportedPlatforms(): Unit = {
    val body = buildRequestBody
    body.put("platform", "GitHub")

    val request  = new PutMethod(OIDC_ENDPOINT)
    val respCode = makeClientRequestWithEntity(request, body)
    assertEquals(HttpStatus.SC_BAD_REQUEST, respCode)
  }

  @Test(description = "Return 403 when user has no permission to access OIDC configuration",
        dependsOnMethods = Array("get"))
  def withoutPermission(): Unit = {
    loginAsLowPrivilegeUser()

    val request = new GetMethod(OIDC_ENDPOINT)

    val respCode = makeClientRequest(request)
    assertEquals(HttpStatus.SC_FORBIDDEN, respCode)
  }
}
