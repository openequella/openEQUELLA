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

import com.tle.admin.graphql.conversion.NameValueViewConverter.toNameValue
import com.tle.admin.helper.GraphQLQueryHelper.getOptionalEntity
import com.tle.common.NameValue
import io.github.openequella.graphql.ClientConfiguration
import io.github.openequella.graphql.api.JavaScriptApi
import org.slf4j.{Logger, LoggerFactory}

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

@Singleton
class AdminJavaScriptServiceImpl @Inject() (implicit
    val cfg: ClientConfiguration
) extends AdminJavaScriptService {

  private implicit val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminJavaScriptServiceImpl])

  override def listLibraries: java.util.List[NameValue] =
    JavaScriptApi.listLibraries match {
      case Right(libraries) => libraries.map(toNameValue).asJava
      case Left(errors)     =>
        throw new ClientRequestException("Error listing JavaScript libraries.", errors)
    }

  override def modulesByLibraryId(
      libraryId: String
  ): java.util.Optional[java.util.List[NameValue]] =
    getOptionalEntity(
      "JavaScript modules [by library ID]",
      libraryId,
      JavaScriptApi.modulesByLibraryId
    )
      .map(_.map(toNameValue).asJava)
      .toJava
}
