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

package com.tle.core.lti13

import com.tle.beans.Institution
import com.tle.common.NameValue
import com.tle.common.filesystem.handle.{SubTemporaryFile, TemporaryFileHandle}
import com.tle.core.guice.Bind
import com.tle.core.institution.convert.ConverterParams
import com.tle.core.institution.convert.service.AbstractJsonConverter
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks
import com.tle.core.lti13.bean.LtiPlatformBean
import com.tle.core.lti13.service.LtiPlatformService
import cats.implicits._
import com.tle.beans.lti.LtiPlatform

import scala.jdk.CollectionConverters._
import javax.inject.{Inject, Singleton}

@Bind
@Singleton
class LtiPlatformConverter extends AbstractJsonConverter[Object] {
  final val EXPORT_FOLDER = "ltiplatform"
  final val ID            = "lti_platform"

  @Inject var ltiPlatformService: LtiPlatformService = _

  override def doExport(staging: TemporaryFileHandle,
                        institution: Institution,
                        callback: ConverterParams): Unit = {
    def writePlatformToJson(platform: LtiPlatformBean): Unit =
      json.write(new SubTemporaryFile(staging, s"$EXPORT_FOLDER/${platform.platformId}"),
                 s"${platform.platformId}.json",
                 LtiPlatformBean)

    Either
      .catchNonFatal {
        ltiPlatformService.getAll.foreach(writePlatformToJson)
      }
      .leftMap(error =>
        throw new RuntimeException(s"Failed to export LTI platforms: ${error.getMessage}"))
  }

  override def doImport(staging: TemporaryFileHandle,
                        institution: Institution,
                        params: ConverterParams): Unit = {
    val dir = new SubTemporaryFile(staging, EXPORT_FOLDER)

    Either
      .catchNonFatal {
        json
          .getFileList(dir)
          .asScala
          .toList
          .map(json.read(dir, _, classOf[LtiPlatformBean]))
          .map(ltiPlatformService.create)
      }
      .leftMap(error =>
        throw new RuntimeException(s"Failed to import LTI platforms: ${error.getMessage}"))
  }

  override def doDelete(institution: Institution, callback: ConverterParams): Unit =
    Either
      .catchNonFatal {
        ltiPlatformService.deleteAll
      }
      .leftMap(error =>
        throw new RuntimeException(s"Failed to delete LTI platforms: ${error.getMessage}"))

  override def addTasks(convertType: ConvertType,
                        tasks: ConverterTasks,
                        params: ConverterParams): Unit =
    tasks.add(new NameValue("LTI platform", "lti_platform"))
}
