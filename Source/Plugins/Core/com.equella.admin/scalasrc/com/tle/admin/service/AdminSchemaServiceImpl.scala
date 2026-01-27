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
import com.tle.admin.graphql.conversion.MetadataSchemaEditViewConverter.toEntityPack
import com.tle.admin.graphql.conversion.MetadataSchemaViewConverter.toSchema
import com.tle.admin.graphql.conversion.{Converter, EntitySkeletonViewConverter}
import com.tle.admin.helper.GraphQLQueryHelper.{getAllUnpaginated, getEntityOrNotFound}
import com.tle.beans.entity.{BaseEntityLabel, Schema}
import com.tle.common.EntityPack
import com.tle.common.beans.exception.NotFoundException
import com.tle.core.remoting.{RemoteAbstractEntityService, RemoteSchemaService}
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.MetadataSchemaApi
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.Inject
import scala.jdk.CollectionConverters._

class AdminSchemaServiceImpl @Inject() (val delegate: RemoteSchemaService)(implicit
    val cfg: ClientConfiguration
) extends AdminEntityService[Schema]
    with AdminSchemaService {
  private implicit val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminSchemaServiceImpl])

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

  override def listAllIncludingSystem(): util.List[BaseEntityLabel] = {
    // For schemas there doesn't seem value in including system ones, so just delegate to listAll.
    // The only system scheme is the "My Content" schema used for Scrapbook items via MyContentService.
    // There's also some ID constants for it in com.tle.mycontent.MyContentConstants.
    listAll()
  }

  override def listAll(): util.List[BaseEntityLabel] =
    MetadataSchemaApi.listSchemas() match {
      case Right(schemas) =>
        schemas
          .map(view => new BaseEntityLabel(view.id, view.uuid, view.bundleId, view.owner))
          .asJava
      case Left(errors) =>
        throw new ClientRequestException(s"Error listing schemas.", errors)
    }

  override def identifyByUuid(uuid: String): Long = MetadataSchemaApi.getIdByUuid(uuid) match {
    case Right(Some(id)) => id
    case Right(None)     => 0
    case Left(errors)    =>
      throw new ClientRequestException(s"Error identifying schema by UUID: $uuid", errors)
  }

  override def exportEntity(id: Long, withSecurity: Boolean): Array[Byte] =
    MetadataSchemaApi.exportSchema(id, withSecurity) match {
      case Right(Some(bytes)) => bytes
      case Right(None)        =>
        throw new NotFoundException(
          s"Schema with ID: $id not found or export failed."
        )
      case Left(errors) =>
        throw new ClientRequestException(s"Error exporting schema with ID: $id", errors)
    }

  override def startEdit(id: Long): EntityPack[Schema] =
    MetadataSchemaApi.startEdit(id) match {
      case Right(schemaEditView) => schemaEditView convert toEntityPack
      case Left(errors)          =>
        throw new ClientRequestException(s"Error starting edit of schema with ID: $id", errors)
    }

  override def startCreate(): EntityPack[Schema] =
    MetadataSchemaApi.startCreate() match {
      case Right(startCreateView) =>
        startCreateView convert EntitySkeletonViewConverter.toEntityPack(new Schema())
      case Left(errors) =>
        throw new ClientRequestException(s"Error starting creation of new schema.", errors)
    }

  override def isStartCreateSupported: Boolean = true

  override def implementMe[T](f: RemoteAbstractEntityService[Schema] => T): T = {
    // TODO: Can this logging be centralise in the abstract class? As it will be the same
    //       for all the overrides.
    LOGGER.warn(
      "Missing implementation of [{}] for RemoteAbstractEntityService, will try delegate.",
      getCallerMethodName,
      new NotImplementedError()
    )
    f(delegate)
  }

  private def getCallerMethodName: String = {
    Thread.currentThread().getStackTrace()(3).getMethodName
  }
}
