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

package com.tle.core.jwks

import com.tle.beans.Institution
import com.tle.beans.webkeyset.WebKeySet
import com.tle.common.NameValue
import com.tle.common.filesystem.handle.{SubTemporaryFile, TemporaryFileHandle}
import com.tle.common.institution.CurrentInstitution
import com.tle.core.guice.Bind
import com.tle.core.institution.convert.service.AbstractJsonConverter
import com.tle.core.institution.convert.ConverterParams
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks
import com.tle.core.webkeyset.service.WebKeySetService
import scala.jdk.CollectionConverters._
import javax.inject.{Inject, Singleton}
import java.time.Instant

/** Case class for the export model.
  */
case class WebKeySetExport(
    keyId: String,
    algorithm: String,
    publicKey: String,
    privateKey: String,
    created: Instant,
    deactivated: Option[Instant]
)

object WebKeySetExport {
  def apply(keySet: WebKeySet): WebKeySetExport =
    WebKeySetExport(
      keyId = keySet.keyId,
      algorithm = keySet.algorithm,
      publicKey = keySet.publicKey,
      privateKey = keySet.privateKey,
      created = keySet.created,
      deactivated = Option(keySet.deactivated)
    )
}

/** This Converter is used to support
  *   1. Exporting RSA keys to JSON files under directory 'keyset'; 2. Reading the JSON files and
  *      importing the keys to the current Institution; 3. Deleting all the keys when the
  *      Institution is deleted.
  */
@Bind
@Singleton
class WebKeySetConverter extends AbstractJsonConverter[Object] {
  final val EXPORT_FOLDER = "keyset"
  final val ID            = "Web_Key_Set"

  @Inject var webKeySetService: WebKeySetService = _

  override def doExport(
      staging: TemporaryFileHandle,
      institution: Institution,
      callback: ConverterParams
  ): Unit =
    webKeySetService.getAll.foreach(keySet =>
      json.write(
        new SubTemporaryFile(staging, s"${EXPORT_FOLDER}/${keySet.algorithm}"),
        s"${keySet.id}.json",
        WebKeySetExport(keySet)
      )
    )

  override def doImport(
      staging: TemporaryFileHandle,
      institution: Institution,
      params: ConverterParams
  ): Unit = {
    val dir = new SubTemporaryFile(staging, EXPORT_FOLDER)
    json
      .getFileList(dir)
      .asScala
      .map(json.read(dir, _, classOf[WebKeySet]))
      .foreach(keySet => {
        keySet.institution = CurrentInstitution.get()
        webKeySetService.createOrUpdate(keySet)
      })
  }

  override def doDelete(institution: Institution, callback: ConverterParams): Unit =
    webKeySetService.deleteAll()

  override def addTasks(
      convertType: ConvertType,
      tasks: ConverterTasks,
      params: ConverterParams
  ): Unit =
    tasks.add(new NameValue("Web key set", "web_key_set"))
}
