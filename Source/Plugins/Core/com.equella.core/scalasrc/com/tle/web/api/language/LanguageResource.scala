package com.tle.web.api.language

import java.util.Locale

import com.tle.legacy.LegacyGuice
import com.tle.web.sections.js.generic.expression.ObjectExpression
import io.circe.parser._
import io.swagger.annotations.Api
import javax.ws.rs.core.{CacheControl, Response}
import javax.ws.rs.{GET, Path, PathParam, Produces}

import scala.io.Source
object LanguageResource
{
  val bundle =
    decode[Map[String, String]](Source.fromResource("lang/jsbundle.json").mkString).fold(throw _, identity)
  val keys = bundle.keySet
}

@Path("language/")
@Api(value = "Languages")
class LanguageResource {

  val cacheHeader = {
    val cc = new CacheControl()
    cc.setPrivate(true)
    cc.setMaxAge(3600)
    cc
  }

  @GET
  @Path("bundle/{locale}/bundle.js")
  @Produces(value=Array("application/javascript"))
  def getBundleOverrides(@PathParam("locale") localeTag: String): Response = {
    val locale = Locale.forLanguageTag(localeTag)
    val bundle = LegacyGuice.languageService.getResourceBundle(locale, "newui")
    val o = new ObjectExpression()
    LanguageResource.keys.foreach {
      k => if (bundle.containsKey(k)) o.put(k, bundle.getString(k))
    }
    Response.ok(s"var bundle = ${o.getExpression(null)};").cacheControl(cacheHeader).build()
  }
}
