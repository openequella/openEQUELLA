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

package com.tle.integration.lti13

import com.tle.common.usermanagement.user.WebAuthenticationDetails
import com.tle.core.services.user.UserService
import com.tle.integration.oauth2.error.general.InvalidJWT
import org.apache.http.HttpStatus
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.GivenWhenThen
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should._

import java.io.{PrintWriter, StringWriter}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._

class OpenIDConnectLaunchServletTest extends AnyFunSpec with Matchers with GivenWhenThen {
  val state   = "5a3f7b2c9d12a4f8"
  val idToken = "not.a.real.token"

  // The servlet's collaborators are wired by Guice field injection, so for testing
  // they are set directly on the (private) fields via reflection.
  private def inject(target: AnyRef, fieldName: String, value: AnyRef): Unit = {
    val field = target.getClass.getDeclaredFields
      .find(_.getName.endsWith(fieldName))
      .getOrElse(throw new NoSuchFieldException(fieldName))
    field.setAccessible(true)
    field.set(target, value)
  }

  private def buildServlet(tokenValidator: Lti13TokenValidator): OpenIDConnectLaunchServlet = {
    val servlet = new OpenIDConnectLaunchServlet

    val userService = mock(classOf[UserService])
    when(userService.getWebAuthenticationDetails(any[HttpServletRequest]()))
      .thenReturn(mock(classOf[WebAuthenticationDetails]))

    inject(servlet, "lti13tokenValidator", tokenValidator)
    inject(servlet, "userService", userService)
    inject(servlet, "stateService", mock(classOf[Lti13StateService]))
    servlet
  }

  describe("an authentication response with an invalid ID token") {
    it("responds with a plain text 'Authentication failed' page") {
      Given("a token validator which rejects the ID token with a general (JWT) error")
      val tokenValidator = mock(classOf[Lti13TokenValidator])
      when(tokenValidator.verifyToken(anyString(), anyString()))
        .thenReturn(Left(Lti13Error.fromGeneralError(InvalidJWT("Provided JWT was invalid"))))
      val servlet = buildServlet(tokenValidator)

      When("an authentication response is POSTed to the servlet")
      val req = mock(classOf[HttpServletRequest])
      when(req.getParameterMap)
        .thenReturn(Map("state" -> Array(state), "id_token" -> Array(idToken)).asJava)

      val resp   = mock(classOf[HttpServletResponse])
      val output = new StringWriter
      when(resp.getWriter).thenReturn(new PrintWriter(output, true))

      servlet.doPost(req, resp)

      Then("the response is a 403 explaining the authentication failure")
      verify(resp).setStatus(HttpStatus.SC_FORBIDDEN)
      output.toString should include("Authentication failed")
      output.toString should include("Provided JWT was invalid")
    }
  }
}
