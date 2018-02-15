package com.tle.web.settings

trait EditableSettings {
  def id: String
  def name: String
  def group: String
  def description: String
  def pageUri: Option[String]
  def isEditable : Boolean
}
