package com.tle.web.api

import cats.effect.IO
import com.tle.core.db._
import com.tle.core.oauthclient.OAuthClientService
import io.swagger.annotations.Api
import javax.ws.rs.core.Response
import javax.ws.rs.{GET, Path, Produces}
import com.softwaremill.sttp._
import com.softwaremill.sttp.circe._

@Api("Testing")
@Path("testing")
class TestOAuthAPI {

  val testUri = uri"http://doolse-sabre:8080/my/api/content/currentuser"

  import io.circe.generic.auto._

  @GET
  @Produces(Array("application/json"))
  def getAToken: Response = {
    RunWithDB.execute {
      OAuthClientService
        .tokenForClient("http://doolse-sabre:8080/my/oauth/access_token",
                        "aadd359a-3478-484f-8aca-97b18901bcd9",
                        "985477ca-52ee-400d-a162-7ad403149352")
        .flatMap { ts =>
          val req = sttp.get(testUri)
          dbLiftIO.liftIO(
            for {
              bodyText <- OAuthClientService.requestWithToken(req, ts.token, ts.tokenType)
            } yield Response.ok(bodyText.unsafeBody).build()
          )
        }
    }
  }
}
