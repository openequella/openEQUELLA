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

import com.tle.beans.entity.{BaseEntityLabel, Schema}
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
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminSchemaServiceImpl])

  override def getSchemaUses(id: Long): util.List[BaseEntityLabel] = withDelegate {
    _.getSchemaUses(id)
  }

  override def getImportSchemaTypes(id: Long): util.List[String] = withDelegate {
    _.getImportSchemaTypes(id)
  }

  override def listEditable(): util.List[BaseEntityLabel] =
    listAll()

  override def listAll(): util.List[BaseEntityLabel] =
    MetadataSchemaApi.listSchemas() match {
      case Right(schemas) =>
        schemas
          .map(view => new BaseEntityLabel(view.id, view.uuid, view.bundleId, view.owner, false))
          .asJava
      case Left(errors) =>
        throw new ClientRequestException(s"Error listing schemas.", errors)
    }

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

  private def withDelegate[T](f: RemoteSchemaService => T): T = {
    LOGGER.warn(
      "Still waiting on GraphQL implementation, will try delegate.",
      new NotImplementedError()
    )
    f(delegate)
  }

  private def getCallerMethodName: String = {
    Thread.currentThread().getStackTrace()(3).getMethodName
  }
}
