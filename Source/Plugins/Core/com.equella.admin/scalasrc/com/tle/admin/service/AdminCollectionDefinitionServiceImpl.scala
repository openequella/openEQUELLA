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
import com.tle.admin.graphql.conversion.Converter
import com.tle.beans.entity.BaseEntityLabel
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.common.beans.exception.NotFoundException
import com.tle.core.remoting.{RemoteAbstractEntityService, RemoteItemDefinitionService}
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.CollectionDefinitionApi
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._

@Singleton
class AdminCollectionDefinitionServiceImpl @Inject() (val delegate: RemoteItemDefinitionService)(
    implicit val cfg: ClientConfiguration
) extends AdminEntityService[ItemDefinition]
    with AdminCollectionDefinitionService {
  private implicit val LOGGER: Logger =
    LoggerFactory.getLogger(classOf[AdminCollectionDefinitionServiceImpl])

  override def enumerateCategories: util.Set[String] = withDelegate {
    _.enumerateCategories()
  }

  override def listUsableItemDefinitionsForSchema(schemaID: Long): util.List[BaseEntityLabel] =
    withDelegate {
      _.listUsableItemDefinitionsForSchema(schemaID)
    }

  override def getSchemaIdForCollectionUuid(value: String): Long = withDelegate {
    _.getSchemaIdForCollectionUuid(value)
  }

  override def exportControl(controlXml: String): Array[Byte] = withDelegate {
    _.exportControl(controlXml)
  }

  override def importControl(zipFileData: Array[Byte]): String = withDelegate {
    _.importControl(zipFileData)
  }

  override def listAll(): util.List[BaseEntityLabel] =
    CollectionDefinitionApi.listCollections() match {
      case Right(collections) => collections.map(toBaseEntityLabel).asJava
      case Left(errors) => throw new ClientRequestException("Error listing collections.", errors)
    }

  override def listEditable(): util.List[BaseEntityLabel] = listAll()

  override def listAllIncludingSystem(): util.List[BaseEntityLabel] = listAll()

  override def identifyByUuid(uuid: String): Long =
    CollectionDefinitionApi.getIdByUuid(uuid) match {
      case Right(Some(id)) => id
      case Right(None)     => 0
      case Left(errors)    =>
        throw new ClientRequestException(
          s"Error identifying collection by UUID: $uuid",
          errors
        )
    }

  override def exportEntity(id: Long, withSecurity: Boolean): Array[Byte] = {
    val exportResult =
      if (withSecurity) CollectionDefinitionApi.exportCollectionWithSecurity(id)
      else CollectionDefinitionApi.exportCollection(id)

    exportResult match {
      case Right(Some(bytes)) => bytes
      case Right(None)        =>
        throw new NotFoundException(s"Collection with ID: $id not found or export failed.")
      case Left(errors) =>
        throw new ClientRequestException(s"Error exporting collection with ID: $id", errors)
    }
  }

  override def cancelEdit(id: Long, force: Boolean): Unit = {
    val cancelEditResult =
      if (force) CollectionDefinitionApi.cancelEditForced(id)
      else CollectionDefinitionApi.cancelEdit(id)

    cancelEditResult match {
      case Right(_)     => // No content expected on success
      case Left(errors) =>
        throw new ClientRequestException(
          s"Error cancelling edit of collection with ID: $id",
          errors
        )
    }
  }

  override def delete(entityid: Long, checkReferences: Boolean): Unit = {
    val deleteResult =
      if (checkReferences) CollectionDefinitionApi.deleteWithReferenceCheck(entityid)
      else CollectionDefinitionApi.delete(entityid)

    deleteResult match {
      case Right(_)     => // No content expected on success
      case Left(errors) =>
        throw new ClientRequestException(
          s"Error deleting collection with ID: $entityid",
          errors
        )
    }
  }

  override def clone(id: Long): BaseEntityLabel =
    CollectionDefinitionApi.clone(id) match {
      case Right(ref)   => ref convert toBaseEntityLabel
      case Left(errors) =>
        throw new ClientRequestException(s"Error cloning collection with ID: $id", errors)
    }

  override def implementMe[T](f: RemoteAbstractEntityService[ItemDefinition] => T): T = {
    logNotImplemented("RemoteAbstractEntityService[ItemDefinition]")
    f(delegate)
  }

  private def withDelegate[T](f: RemoteItemDefinitionService => T): T = {
    logNotImplemented("RemoteItemDefinitionService")
    f(delegate)
  }

  private def logNotImplemented(forInterface: String): Unit = {
    LOGGER.warn(
      "Missing implementation of [{}] for {}, will try delegate.",
      getCallerMethodName,
      forInterface,
      new NotImplementedError()
    )
  }

  private def getCallerMethodName: String = {
    Thread.currentThread().getStackTrace()(4).getMethodName
  }
}
