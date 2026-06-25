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

import com.tle.admin.graphql.conversion.BaseEntityReferenceViewConverter.toBaseEntityLabel
import com.tle.admin.graphql.conversion.MetadataSchemaEditViewConverter.{
  fromEntityPack,
  toEntityPack
}
import com.tle.admin.graphql.conversion.MetadataSchemaViewConverter.toSchema
import com.tle.admin.graphql.conversion.{Converter, EntitySkeletonViewConverter}
import com.tle.admin.helper.GraphQLQueryHelper.{getAllUnpaginated, getEntityOrNotFound}
import com.tle.beans.entity.{BaseEntityLabel, Schema}
import com.tle.common.EntityPack
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.MetadataSchemaApi
import io.github.openequella.graphql.api.views.MetadataSchemaEditView
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
class AdminSchemaServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration
) extends AdminEntityService[Schema]
    with AdminSchemaService {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminSchemaServiceImpl])

  override def entityDescription: String = "schema"

  override def get(id: Long): Schema =
    getEntityOrNotFound("Schema [by id]", id, MetadataSchemaApi.getById) convert toSchema

  override def getSchemaUses(id: Long): util.List[BaseEntityLabel] =
    getAllUnpaginated("Schema uses", id, MetadataSchemaApi.getUses).map(toBaseEntityLabel).asJava

  override def hasReferencingClasses(id: Long): Boolean =
    MetadataSchemaApi.hasReferences(id) match {
      case Right(hasRefs) => hasRefs
      case Left(errors)   =>
        throw new ClientRequestException(
          s"Error checking references for schema with ID: $id",
          errors
        )
    }

  override def getImportSchemaTypes(id: Long): util.List[String] =
    new util.ArrayList[
      String
    ]( // to provide a mutable collection for the Java side to do List.addFirst
      getAllUnpaginated("Schema import types", id, MetadataSchemaApi.getImportTypes).asJava
    )

  override def listEditable(): util.List[BaseEntityLabel] =
    listAll()

  override def listAllIncludingSystem(): util.List[BaseEntityLabel] =
    listAll()

  override def listAll(): util.List[BaseEntityLabel] =
    listAllFrom(MetadataSchemaApi.listSchemas())

  override def identifyByUuid(uuid: String): Long =
    idByUuid(MetadataSchemaApi.getIdByUuid)(uuid)

  override def exportEntity(id: Long, withSecurity: Boolean): Array[Byte] =
    exportWith(MetadataSchemaApi.exportSchema, MetadataSchemaApi.exportSchemaWithSecurity)(
      id,
      withSecurity
    )

  override def importEntity(zip: Array[Byte]): EntityPack[Schema] =
    importWith(MetadataSchemaApi.importSchema)(_ convert toEntityPack)(zip)

  override def startEdit(id: Long): EntityPack[Schema] =
    MetadataSchemaApi.startEdit(id) match {
      case Right(schemaEditView) => schemaEditView convert toEntityPack
      case Left(errors)          =>
        throw new ClientRequestException(s"Error starting edit of schema with ID: $id", errors)
    }

  override def startCreate(): EntityPack[Schema] =
    MetadataSchemaApi.startCreate() match {
      case Right(startCreateView) =>
        startCreateView convert EntitySkeletonViewConverter.toEntityPack(new Schema)
      case Left(errors) =>
        throw new ClientRequestException(s"Error starting creation of new schema.", errors)
    }

  override def cancelEdit(id: Long, force: Boolean): Unit =
    cancelEditWith(MetadataSchemaApi.cancelEdit, MetadataSchemaApi.cancelEditForced)(id, force)

  override def stopEdit(pack: EntityPack[Schema], unlock: Boolean): Schema = {
    val details        = pack convert fromEntityPack
    val stopEditResult =
      if (unlock) MetadataSchemaApi.stopEditAndUnlock(details)
      else MetadataSchemaApi.stopEdit(details)

    stopEditResult match {
      case Right(updatedView) => updatedView convert toSchema
      case Left(errors)       =>
        throw new ClientRequestException(
          s"Error saving changes for schema with ID: ${pack.getEntity.getId}",
          errors
        )
    }
  }

  override def delete(entityid: Long, checkReferences: Boolean): Unit =
    deleteWith(MetadataSchemaApi.delete, MetadataSchemaApi.deleteWithReferenceCheck)(
      entityid,
      checkReferences
    )

  override def add(pack: EntityPack[Schema], lockAfterwards: Boolean): BaseEntityLabel = {
    val details: MetadataSchemaEditView = pack convert fromEntityPack
    val addResult                       =
      if (lockAfterwards) MetadataSchemaApi.addAndLock(details)
      else MetadataSchemaApi.add(details)

    addResult match {
      case Right(ref)   => ref convert toBaseEntityLabel
      case Left(errors) =>
        throw new ClientRequestException(s"Error adding new schema.", errors)
    }
  }

  override def clone(id: Long): BaseEntityLabel =
    cloneWith(MetadataSchemaApi.clone)(id)

  override def isStartCreateSupported: Boolean = true
}
