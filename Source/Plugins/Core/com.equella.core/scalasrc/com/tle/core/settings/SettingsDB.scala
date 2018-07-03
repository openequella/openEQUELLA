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

package com.tle.core.settings

import cats.data.{Kleisli, OptionT}
import cats.syntax.applicative._
import com.tle.core.db.{DB, UserContext}
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.doolse.simpledba.WriteQueries
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._
import fs2.Stream

case class Setting(institution_id: Long, property: String, value: String)

case class SettingsQueries[F[_]](write: WriteQueries[F, Setting], query: ((Long, String)) => Stream[F, Setting])


object SettingsDB {

  import io.doolse.simpledba.jdbc.postgres._
  implicit val dbConfig = postgresConfig
  val settingsRel = TableMapper[Setting].table("configuration_property").key('institution_id, 'property)

  val q : SettingsQueries[JDBCIO] = SettingsQueries(settingsRel.writes, settingsRel.byPK)

  def singleProperty(name: String): OptionT[DB, Setting] = OptionT {
    Kleisli {
      case UserContext(inst, _, _) => q.query(inst.getDatabaseId, name).compile.last
    }
  }

  def jsonProperty[A](name: String)(implicit dec: Decoder[A]): OptionT[DB, A] = singleProperty(name).map {
    s => parse(s.value).flatMap(dec.decodeJson).fold(throw _, identity)
  }

  def mkSetting(name: String, value: String): DB[Setting] = Kleisli {
    case UserContext(inst, _, _) => Setting(inst.getDatabaseId, name, value).pure[JDBCIO]
  }

  def setJsonProperty[A : Encoder : Decoder](name: String, value: A): DB[Unit] = {
    val newJson = value.asJson.noSpaces
    for {
      newSetting <- mkSetting(name, newJson)
      existProp <- singleProperty(name).value
      _ <- Kleisli.liftF {
        (existProp match {
          case None => q.write.insert(newSetting)
          case Some(existing) => q.write.update(existing, newSetting)
        }).flush.compile.drain
      }
    } yield ()
  }

}
