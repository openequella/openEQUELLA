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

import com.tle.beans.entity.BaseEntityLabel
import com.tle.beans.entity.itemdef.ItemDefinition
import com.tle.core.remoting.{RemoteAbstractEntityService, RemoteItemDefinitionService}
import io.github.openequella.graphql.ClientConfiguration
import org.slf4j.{Logger, LoggerFactory}

import java.util
import javax.inject.{Inject, Singleton}

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
