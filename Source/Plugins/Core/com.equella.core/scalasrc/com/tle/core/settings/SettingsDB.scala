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

package com.tle.core.settings

import cats.data.{Kleisli, OptionT}
import cats.syntax.applicative._
import com.tle.core.db.tables.Setting
import com.tle.core.db._
import com.tle.core.security.AclChecks
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import io.doolse.simpledba.jdbc._
import io.doolse.simpledba.syntax._
import fs2.Stream

object SettingsDB {

  def ensureEditSystem[A](db: DB[A]): DB[A] = AclChecks.ensureOnePriv("EDIT_SYSTEM_SETTINGS")(db)

  private val q = DBSchema.queries.settingsQueries

  def singleProperty(name: String): OptionT[DB, Setting] = OptionT {
    Kleisli { uc =>
      q.query(uc.inst, name).compile.last
    }
  }

  def multiProperties(prefix: String): Stream[DB, Setting] =
    dbStream(uc => q.prefixQuery(uc.inst, prefix))

  def decodeSetting[A](f: Setting => A => A)(s: Setting)(implicit dec: Decoder[A]): A =
    parse(s.value).flatMap(dec.decodeJson).fold(throw _, f(s))

  def jsonProperty[A: Decoder](name: String): OptionT[DB, A] =
    singleProperty(name).map(decodeSetting[A](_ => identity))

  def jsonProperties[A: Decoder](prefix: String, f: String => A => A): Stream[DB, A] =
    multiProperties(prefix + "%").map(decodeSetting[A](s => f(s.property.substring(prefix.length))))

  def mkSetting(name: String, value: String): DB[Setting] = Kleisli { uc =>
    Setting(uc.inst, name, value).pure[JDBCIO]
  }

  def setJsonProperty[A: Encoder: Decoder](name: String, value: A): DB[Unit] = {
    val newJson = value.asJson.noSpaces
    for {
      newSetting <- mkSetting(name, newJson)
      existProp  <- singleProperty(name).value
      _ <- Kleisli.liftF {
        (existProp match {
          case None           => q.write.insert(newSetting)
          case Some(existing) => q.write.update(existing, newSetting)
        }).flush.compile.drain
      }
    } yield ()
  }

}
