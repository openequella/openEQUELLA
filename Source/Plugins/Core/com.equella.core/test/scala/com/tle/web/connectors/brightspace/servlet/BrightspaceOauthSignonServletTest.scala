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

package com.tle.web.connectors.brightspace.servlet

import com.fasterxml.jackson.databind.ObjectMapper
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService
import com.tle.core.institution.InstitutionService
import com.tle.core.services.user.UserSessionService
import org.mockito.ArgumentMatchers.{anyInt, anyString}
import org.mockito.Mockito.{mock, never, verify, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._

import java.net.URI
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

class BrightspaceOauthSignonServletTest extends AnyFunSpec with Matchers {

  private val InstitutionUrl = "https://oeq.example.com/inst1/"

  /** Invokes the servlet's `doGet` to simulate the request received when a user is forwarded back
    * from the Brightspace OAuth sign-on flow.
    *
    * @param forwardUrl
    *   the forward URL carried in the decrypted state
    */
  private def doGet(forwardUrl: Option[String]): HttpServletResponse = {
    val stateParam     = "x_state"
    val encryptedState = "encrypted-state"

    val mapper = new ObjectMapper

    // The institution base URL that forward URLs are resolved against and checked for safety.
    val institutionService = mock(classOf[InstitutionService])
    when(institutionService.getInstitutionUri).thenReturn(new URI(InstitutionUrl))

    // Decryption yields the state JSON. Include the "forwardUrl" field only when one is supplied,
    // so None models a callback that carries no forward URL (the dialog-close path).
    val connectorService = mock(classOf[BrightspaceConnectorService])
    val stateJson        = mapper.createObjectNode()
    forwardUrl.foreach(url => stateJson.put("forwardUrl", url))
    when(connectorService.decrypt(encryptedState)).thenReturn(stateJson.toString)

    val servlet = new BrightspaceOauthSignonServlet(
      mock(classOf[UserSessionService]),
      connectorService,
      institutionService
    )

    // The request only needs to expose the encrypted state parameter; the user id/key params are
    // read via getParameter and default to null, which is fine for these tests.
    val req = mock(classOf[HttpServletRequest])
    when(req.getParameter(stateParam)).thenReturn(encryptedState)

    // Return the response mock so each test can verify sendRedirect / sendError on it.
    val resp = mock(classOf[HttpServletResponse])
    servlet.doGet(req, resp)
    resp
  }

  describe("doGet") {
    it("redirects to the institution-resolved URL when the forward URL is safe") {
      val cases = Table(
        ("name", "forwardUrl", "resolvedRedirect"),
        (
          "absolute URL on institution",
          "https://oeq.example.com/inst1/somepage?x=1",
          "https://oeq.example.com/inst1/somepage?x=1"
        ),
        ("relative URL", "somepage?x=1", "https://oeq.example.com/inst1/somepage?x=1"),
        (
          "absolute path on institution",
          "/inst1/otherpage",
          "https://oeq.example.com/inst1/otherpage"
        )
      )

      forAll(cases) { (_, forwardUrl, resolvedRedirect) =>
        val resp = doGet(Some(forwardUrl))
        verify(resp).sendRedirect(resolvedRedirect)
        verify(resp, never()).sendError(anyInt(), anyString())
      }
    }

    it("rejects with a 400 when the forward URL leaves the institution or is malformed") {
      val cases = Table(
        ("name", "forwardUrl"),
        ("absolute URL to another host", "https://attacker.example/phish"),
        ("absolute path on another institution", "/inst2/otherpage"),
        ("protocol-relative URL", "//attacker.example/phish"),
        ("javascript scheme", "javascript:alert(1)"),
        ("data scheme", "data:text/html,<script>alert(1)</script>"),
        ("different port on same host", "https://oeq.example.com:8443/inst1/somepage"),
        ("path outside institution prefix", "https://oeq.example.com/otherinst/somepage"),
        ("path traversal escaping the institution", "/inst1/../otherinst"),
        ("malformed URL", "ht!tp://[invalid"),
        ("scheme downgrade with matching default port", "http://oeq.example.com:443/inst1/somepage")
      )

      forAll(cases) { (_, forwardUrl) =>
        val resp = doGet(Some(forwardUrl))
        verify(resp).sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid forward URL")
        verify(resp, never()).sendRedirect(anyString())
      }
    }

    it("neither redirects nor errors when there is no forward URL (dialog just closes)") {
      val resp = doGet(None)
      verify(resp, never()).sendRedirect(anyString())
      verify(resp, never()).sendError(anyInt(), anyString())
    }
  }
}
