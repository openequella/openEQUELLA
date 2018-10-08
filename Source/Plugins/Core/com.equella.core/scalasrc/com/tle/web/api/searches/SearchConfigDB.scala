package com.tle.web.api.searches

import java.util.UUID

import cats.data.OptionT
import com.tle.core.db.DB
import com.tle.core.settings.SettingsDB

object SearchConfigDB {


  def configName(id: UUID): String = s"searchconfig.$id"
  def pageConfigName(name: String): String = s"searchpage.$name"

  def writeConfig(id: UUID, config: SearchConfig): DB[Unit] =
    SettingsDB.setJsonProperty(configName(id), config)

  def readConfig(id: UUID) : OptionT[DB, SearchConfig] =
    SettingsDB.jsonProperty(configName(id))

  def readPageConfig(page: String): OptionT[DB, SearchPageConfig] =
    SettingsDB.jsonProperty(pageConfigName(page))

  def writePageConfig(page: String, config: SearchPageConfig): DB[Unit] =
    SettingsDB.setJsonProperty(pageConfigName(page), config)
}
