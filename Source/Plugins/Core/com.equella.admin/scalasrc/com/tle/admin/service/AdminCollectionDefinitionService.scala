package com.tle.admin.service

import com.tle.beans.entity.BaseEntityLabel
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.core.remoting.RemoteAbstractEntityService

import java.util

trait AdminCollectionDefinitionService extends RemoteAbstractEntityService[ItemDefinition] {
  def enumerateCategories: util.Set[String]

  def listUsableItemDefinitionsForSchema(schemaID: Long): util.List[BaseEntityLabel]

  def getSchemaIdForCollectionUuid(value: String): Long

  def exportControl(controlXml: String): Array[Byte]

  def importControl(zipFileData: Array[Byte]): String
}

object AdminCollectionDefinitionService {
  val ENTITY_TYPE: String     = "COLLECTION"
  val ATTRIBUTE_KEY_FILESTORE = "filestore.location"
}
