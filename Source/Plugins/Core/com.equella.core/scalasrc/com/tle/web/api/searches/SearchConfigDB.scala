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

package com.tle.web.api.searches

import java.util.{Date, Locale, UUID}

import cats.data.{OptionT, ValidatedNec}
import cats.syntax.functor._
import cats.syntax.validated._
import com.tle.core.db._
import com.tle.core.db.dao.{EntityDB, EntityDBExt}
import com.tle.core.db.tables.OEQEntity
import com.tle.core.settings.SettingsDB
import com.tle.core.validation.EntityValidation
import fs2.Stream
import io.circe.generic.semiauto._
import io.doolse.simpledba.Iso
import io.doolse.simpledba.circe._

case class SearchConfigDB(entity: OEQEntity, data: SearchConfigData)

case class SearchConfigData(index: String, sections: Map[String, Iterable[SearchControl]])

object SearchConfigData {
  implicit val decoder = deriveDecoder[SearchConfigData]
  implicit val encoder = deriveEncoder[SearchConfigData]
  val empty            = SearchConfigData("", Map.empty)
}

object SearchConfigDB {

  type SearchConfigVal[A] = ValidatedNec[EntityValidation, A]

  implicit val cd: EntityDBExt[SearchConfigDB] =
    new EntityDBExt[SearchConfigDB] {
      val dataIso = circeJsonUnsafe[SearchConfigData]
      val iso = Iso(
        oeq => SearchConfigDB(oeq, dataIso.from(oeq.data)),
        scdb => scdb.entity.copy(data = dataIso.to(scdb.data))
      )

      override def typeId: String = "searchconfig"
    }

  private def toSearchConfig(locale: Locale)(scdb: SearchConfigDB): SearchConfig = {
    val oeq = scdb.entity
    val scd = scdb.data
    SearchConfig(
      oeq.uuid.id,
      scd.index,
      oeq.name_strings.closest(locale).getOrElse(oeq.name),
      oeq.name_strings.stringsOrNone,
      oeq.description_strings.closest(locale),
      oeq.description_strings.stringsOrNone,
      Date.from(oeq.created),
      Date.from(oeq.modified),
      scd.sections
    )
  }

  def pageConfigName(name: String): String = s"searchpage.$name"

  def editFields(
      original: SearchConfigDB,
      edits: SearchConfigEdit,
      locale: Locale
  ): SearchConfigVal[SearchConfigDB] =
    EntityValidation.standardValidation(edits, original.entity, locale).map { newoeq =>
      SearchConfigDB(newoeq, original.data.copy(index = edits.index, sections = edits.sections))
    }

  def createConfig(id: UUID, newConfig: SearchConfigEdit): DB[SearchConfigVal[Unit]] =
    for {
      locale <- getContext.map(_.locale)
      oeq    <- EntityDB.newEntity(id)
      valid <- editFields(SearchConfigDB(oeq, SearchConfigData.empty), newConfig, locale)
        .traverse(flushDB.compose(EntityDB.create[SearchConfigDB]))
    } yield valid

  def deleteConfig(id: UUID): DB[Unit] =
    EntityDB.delete(id).compile.drain

  // The Boolean is whether or not the entity existed already
  def editConfig(id: UUID, edits: SearchConfigEdit): DB[SearchConfigVal[Boolean]] =
    getContext.map(_.locale).flatMap { locale =>
      EntityDB
        .readOne[SearchConfigDB](id)
        .semiflatMap { orig =>
          editFields(orig, edits, locale).traverse { edited =>
            flushDB(EntityDB.update[SearchConfigDB](orig.entity, edited)).as(true)
          }
        }
        .getOrElse(false.valid)
    }

  def readAllConfigs: Stream[DB, SearchConfig] = Stream.eval(getContext).flatMap { uc =>
    EntityDB.readAll[SearchConfigDB].map(toSearchConfig(uc.locale))
  }

  def readConfig(id: UUID): OptionT[DB, SearchConfig] = OptionT.liftF(getContext).flatMap { uc =>
    EntityDB.readOne[SearchConfigDB](id).map(toSearchConfig(uc.locale))
  }

  def readPageConfig(page: String): OptionT[DB, SearchPageConfig] =
    SettingsDB.jsonProperty(pageConfigName(page))

  def writePageConfig(page: String, config: SearchPageConfig): DB[Unit] =
    SettingsDB.setJsonProperty(pageConfigName(page), config)
}
