package com.tle.web.settings.rest

import java.net.URI
import java.util
import javax.ws.rs.{GET, Path}

import com.tle.common.institution.CurrentInstitution
import com.tle.web.settings.SettingsList

import scala.collection.JavaConverters._
import io.swagger.annotations.Api

import scala.beans.BeanProperty

case class SettingTypeLinks(@BeanProperty web: URI)
case class SettingType(@BeanProperty id: String, @BeanProperty name: String, @BeanProperty description: String,
                       @BeanProperty group: String, @BeanProperty links: SettingTypeLinks)

@Path("settings/")
@Api(value = "Settings")
class SettingsResource {

  @GET
  def settings : util.Collection[SettingType] = {
    SettingsList.allSettings.filter(_.isEditable).map { s =>
      val instUri = CurrentInstitution.get().getUrlAsUri
      SettingType(s.id, s.name, s.description, s.group, SettingTypeLinks(instUri.resolve(s.pageUri.orNull)))
    }.asJavaCollection
  }
}
