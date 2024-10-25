package io.github.openequella.graphql.test

import io.github.openequella.graphql.api.ApiError
import io.github.openequella.graphql.{Client, ClientConfiguration}
import org.scalatest.Assertions.fail
import org.scalatest.matchers.must.Matchers.have
import org.scalatest.matchers.should.Matchers.{a, convertToAnyShouldWrapper}
import sttp.model.Uri

object TestHelper {
  val CREDENTIALS_AUTOTEST: (String, String) = ("AutoTest", "automated")
  val CREDENTIALS_ADMIN: (String, String)    = ("TLE_ADMINISTRATOR", "autotestpassword")

  /**
    * Login to the specified institution with the given credentials.
    *
    * @param institution the institution to log in to, which will be used to build an institution URL
    *                    of the form http://localhost:8080/institution
    * @param credentials the credentials to use (typically one of the constants defined in this object)
    * @return the client configuration if successful, or an error message if not
    */
  def login(institution: String, credentials: (String, String)): ClientConfiguration = {
    val instUrl                           = Uri("localhost").port(8080).withPath(institution)
    implicit val cfg: ClientConfiguration = ClientConfiguration(instUrl)

    Client.login(credentials._1, credentials._2) match {
      case Right(_)  => cfg
      case Left(err) => fail(err._2)
    }
  }

  /**
    * Check that the response is an error response, and return the error. This can then be used to
    * check the specific error type.
    *
    * @param response the response to check
    * @return the error if available, otherwise `fail()`
    */
  def checkApiError(response: Either[List[ApiError], _]): ApiError = {
    response shouldBe a[Left[List[ApiError], _]]
    response.swap.foreach { errors =>
      errors should have length 1
    }
    response match {
      case Left(errors) => errors.head
      case Right(_)     => fail("Expected an error response")
    }
  }
}
