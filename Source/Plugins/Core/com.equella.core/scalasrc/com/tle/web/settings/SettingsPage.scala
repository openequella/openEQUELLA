package com.tle.web.settings

import com.tle.web.resources.PluginResourceHelper

case class SettingsPage(helper: PluginResourceHelper, id: String, nameKey: String, descKey: String, page: String, editable: () => Boolean) extends EditableSettings
{
  override def name: String = helper.getString(nameKey)

  override def description: String = helper.getString(descKey)

  override def pageUri: Option[String] = Some(page)

  def group = "general"

  def isEditable = editable()
}
