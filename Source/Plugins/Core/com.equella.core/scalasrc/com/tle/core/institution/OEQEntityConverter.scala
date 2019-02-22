/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.institution

import java.time.Instant
import java.util.UUID

import cats.data.Kleisli
import com.tle.beans.Institution
import com.tle.common.NameValue
import com.tle.common.filesystem.handle.TemporaryFileHandle
import com.tle.common.institution.CurrentInstitution
import com.tle.core.db._
import com.tle.core.db.dao.EntityDB
import com.tle.core.db.tables.OEQEntity
import com.tle.core.db.types.{DbUUID, InstId, LocaleStrings, UserId}
import com.tle.core.institution.convert.service.InstitutionImportService
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl
import com.tle.core.institution.convert.{Converter, ConverterParams}
import fs2.Pipe
import io.circe.Json
import io.doolse.simpledba.jdbc._
import cats.syntax.apply._
import com.tle.core.i18n.CoreStrings

object OEQEntityConverter {
  def TaskId         = "oeqentity"
  def BaseExportPath = "entity"
}

class OEQEntityConverter extends Converter {
  import OEQEntityConverter._
  import io.circe.generic.semiauto._

  case class OEQEntityJson(
      uuid: UUID,
      typeid: String,
      name: String,
      nameStrings: Map[String, String],
      description: Option[String],
      descriptionStrings: Map[String, String],
      owner: String,
      created: Long,
      modified: Long,
      data: Json
  )

  val oeqEncoder = deriveEncoder[OEQEntityJson]
  val oeqDecoder = deriveDecoder[OEQEntityJson]

  def toEntityJson(oeq: OEQEntity): OEQEntityJson = {
    OEQEntityJson(
      oeq.uuid.id,
      oeq.typeid,
      oeq.name,
      oeq.name_strings.strings,
      oeq.description,
      oeq.description_strings.strings,
      oeq.owner.id,
      oeq.created.toEpochMilli,
      oeq.modified.toEpochMilli,
      oeq.data
    )
  }
  def fromEntityJson(inst: InstId)(j: OEQEntityJson): OEQEntity = {
    OEQEntity(
      DbUUID(j.uuid),
      inst,
      j.typeid,
      j.name,
      LocaleStrings(j.nameStrings),
      j.description,
      LocaleStrings(j.descriptionStrings),
      UserId(j.owner),
      Instant.ofEpochMilli(j.created),
      Instant.ofEpochMilli(j.modified),
      j.data
    )
  }

  def jsonPipe(staging: TemporaryFileHandle): Pipe[JDBCIO, OEQEntityJson, Unit] =
    ExportUtils.asJsonFiles(
      oeqEncoder,
      (e: OEQEntityJson) => s"$BaseExportPath/${e.typeid}/${e.uuid}.json",
      staging
    )

  override def doInTransaction(runnable: Runnable): Unit = {}

  override def addTasks(
      `type`: InstitutionImportService.ConvertType,
      tasks: InstitutionImportServiceImpl.ConverterTasks,
      params: ConverterParams
  ): Unit = {
    tasks.add(new NameValue(CoreStrings.text("impexp.entities"), OEQEntityConverter.TaskId))
  }

  override def deleteIt(
      staging: TemporaryFileHandle,
      institution: Institution,
      params: ConverterParams,
      task: String
  ): Unit = {
    RunWithDB.execute {
      flushDB {
        EntityDB.queries.write.deleteAll(EntityDB.queries.allByInst(institution))
      }
    }
  }

  override def clone(
      staging: TemporaryFileHandle,
      newInstitution: Institution,
      params: ConverterParams,
      task: String
  ): Unit = {
    RunWithDB.executeWithHibernate {
      exportDB(staging, CurrentInstitution.get()) *>
        importDB(staging, newInstitution)
    }
  }

  def exportDB(staging: TemporaryFileHandle, institution: Institution): DB[Unit] =
    Kleisli.liftF {
      jsonPipe(staging) {
        EntityDB.queries.allByInst(institution).map(toEntityJson)
      }.compile.drain
    }

  def importDB(staging: TemporaryFileHandle, newInstitution: Institution): DB[Unit] = {
    flushDB {
      EntityDB.queries.write
        .insertAll {
          ExportUtils
            .jsonFileStream[OEQEntityJson, JDBCIO](oeqDecoder, staging, BaseExportPath)
            .map(fromEntityJson(newInstitution))
        }
    }
  }

  override def exportIt(
      staging: TemporaryFileHandle,
      institution: Institution,
      params: ConverterParams,
      cid: String
  ): Unit = {
    RunWithDB.execute { exportDB(staging, institution) }
  }

  override def importIt(
      staging: TemporaryFileHandle,
      newInstitution: Institution,
      params: ConverterParams,
      cid: String
  ): Unit = {
    RunWithDB.executeWithHibernate { importDB(staging, newInstitution) }
  }
}
