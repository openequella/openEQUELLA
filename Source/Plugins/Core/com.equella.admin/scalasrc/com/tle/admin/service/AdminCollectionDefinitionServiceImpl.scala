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

package com.tle.admin.service

import com.tle.admin.controls.ExportedControlZip
import com.tle.admin.graphql.conversion.BaseEntityReferenceViewConverter.toBaseEntityLabel
import com.tle.admin.graphql.conversion.CollectionDefinitionEditViewConverter.{
  fromEntityPack,
  toEntityPack
}
import com.tle.admin.graphql.conversion.CollectionDefinitionViewConverter.toItemDefinition
import com.tle.admin.graphql.conversion.{Converter, EntitySkeletonViewConverter}
import com.tle.admin.helper.GraphQLQueryHelper.{
  executeOrThrow,
  getAllUnpaginated,
  getOptionalEntityOrNotFound
}
import com.tle.admin.rest.RestConfiguration
import com.tle.beans.entity.BaseEntityLabel
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.common.EntityPack
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.CollectionDefinitionApi
import io.github.openequella.graphql.api.views.CollectionDefinitionEditView
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
class AdminCollectionDefinitionServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration,
    val restCfg: RestConfiguration
) extends AdminEntityService[ItemDefinition]
    with AdminCollectionDefinitionService {
  private implicit val LOGGER: Logger =
    LoggerFactory.getLogger(classOf[AdminCollectionDefinitionServiceImpl])

  override def entityDescription: String = "collection"

  override def get(id: Long): ItemDefinition =
    getOptionalEntityOrNotFound(
      "Collection [by id]",
      id,
      CollectionDefinitionApi.getById
    ) convert toItemDefinition

  override def getByUuid(uuid: String): ItemDefinition =
    getOptionalEntityOrNotFound(
      "Collection [by uuid]",
      uuid,
      CollectionDefinitionApi.getByUuid
    ) convert toItemDefinition

  // The server already returns these sorted and de-duplicated, so no further ordering is applied.
  override def enumerateCategories: util.List[String] =
    executeOrThrow("listing collection wizard categories")(
      CollectionDefinitionApi.listCategories()
    ).asJava

  override def listUsableItemDefinitionsForSchema(schemaID: Long): util.List[BaseEntityLabel] =
    getAllUnpaginated("collections for schema", schemaID, CollectionDefinitionApi.listForSchema)
      .map(toBaseEntityLabel)
      .asJava

  // Flattening the schema ID out of the collection means a missing collection and a collection with
  // no schema are indistinguishable, and both raise NotFoundException. The legacy implementation
  // threw an NPE in both cases, so no caller could have told them apart either.
  override def getSchemaIdForCollectionUuid(value: String): Long =
    getOptionalEntityOrNotFound(
      "Collection schema ID [by collection uuid]",
      value,
      (uuid: String) => CollectionDefinitionApi.getByUuid(uuid).map(_.flatMap(_.schemaId))
    )

  override def exportControl(controlXml: String): Array[Byte] =
    ExportedControlZip.zip(controlXml)

  override def importControl(zipFileData: Array[Byte]): String =
    ExportedControlZip.unzip(zipFileData)

  override def listAll(): util.List[BaseEntityLabel] =
    listAllFrom(CollectionDefinitionApi.listCollections())

  override def listEditable(): util.List[BaseEntityLabel] = listAll()

  override def listAllIncludingSystem(): util.List[BaseEntityLabel] = listAll()

  override def identifyByUuid(uuid: String): Long =
    idByUuid(CollectionDefinitionApi.getIdByUuid)(uuid)

  override def exportEntity(id: Long, withSecurity: Boolean): Array[Byte] =
    exportWith(
      CollectionDefinitionApi.exportCollection,
      CollectionDefinitionApi.exportCollectionWithSecurity
    )(id, withSecurity)

  override def importEntity(zip: Array[Byte]): EntityPack[ItemDefinition] =
    importWith(CollectionDefinitionApi.importCollection)(_ convert toEntityPack)(zip)

  override def cancelEdit(id: Long, force: Boolean): Unit =
    cancelEditWith(CollectionDefinitionApi.cancelEdit, CollectionDefinitionApi.cancelEditForced)(
      id,
      force
    )

  override def delete(entityid: Long, checkReferences: Boolean): Unit =
    deleteWith(CollectionDefinitionApi.delete, CollectionDefinitionApi.deleteWithReferenceCheck)(
      entityid,
      checkReferences
    )

  override def add(pack: EntityPack[ItemDefinition], lockAfterwards: Boolean): BaseEntityLabel = {
    val details: CollectionDefinitionEditView = pack convert fromEntityPack

    executeOrThrow("adding new collection")(
      if (lockAfterwards) CollectionDefinitionApi.addAndLock(details)
      else CollectionDefinitionApi.add(details)
    ) convert toBaseEntityLabel
  }

  override def clone(id: Long): BaseEntityLabel =
    cloneWith(CollectionDefinitionApi.clone)(id)

  override def startEdit(id: Long): EntityPack[ItemDefinition] =
    startEditWith(CollectionDefinitionApi.startEdit)(_ convert toEntityPack)(id)

  override def startCreate(): EntityPack[ItemDefinition] =
    executeOrThrow("starting creation of new collection")(
      CollectionDefinitionApi.startCreate()
    ) convert EntitySkeletonViewConverter.toEntityPack(new ItemDefinition)

  override def stopEdit(pack: EntityPack[ItemDefinition], unlock: Boolean): ItemDefinition =
    stopEditWith(CollectionDefinitionApi.stopEdit, CollectionDefinitionApi.stopEditAndUnlock)(
      _ convert fromEntityPack
    )(_ convert toItemDefinition)(pack, unlock)

  override def isStartCreateSupported: Boolean = true
}
