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

package com.tle.core.cloudproviders

import java.util.concurrent.TimeUnit
import java.util.{Locale, UUID}

import fs2._
import cats.syntax.apply._
import cats.syntax.functor._
import cats.data.Validated.{Invalid, Valid}
import cats.data.{OptionT, ValidatedNec}
import cats.effect.{IO, LiftIO}
import cats.syntax.validated._

import cats.syntax.applicative._
import com.tle.core.db._
import com.tle.core.db.dao.{EntityDB, EntityDBExt}
import com.tle.core.db.tables.OEQEntity
import com.tle.core.validation.{EntityValidation, OEQEntityEdits}
import com.tle.legacy.LegacyGuice
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.doolse.simpledba.Iso
import io.doolse.simpledba.circe.circeJsonUnsafe

case class CloudProviderData(baseUrl: String,
                             iconUrl: Option[String],
                             providerAuth: CloudOAuthCredentials,
                             oeqAuth: CloudOAuthCredentials,
                             serviceUris: Map[String, ServiceUri],
                             viewers: Map[String, Map[String, Viewer]])

object CloudProviderData {

  import io.circe.generic.auto._

  implicit val decoder = deriveDecoder[CloudProviderData]
  implicit val encoder = deriveEncoder[CloudProviderData]
}

case class CloudProviderDB(entity: OEQEntity, data: CloudProviderData)

object CloudProviderDB {

  val tokenCache =
    LegacyGuice.replicatedCacheService.getCache[String]("cloudRegTokens", 100, 1, TimeUnit.HOURS)

  type CloudProviderVal[A] = ValidatedNec[EntityValidation, A]

  implicit val dbExt: EntityDBExt[CloudProviderDB] =
    new EntityDBExt[CloudProviderDB] {
      val dataIso = circeJsonUnsafe[CloudProviderData]
      val iso = Iso(
        oeq => CloudProviderDB(oeq, dataIso.from(oeq.data)),
        scdb => scdb.entity.copy(data = dataIso.to(scdb.data))
      )

      override def typeId: String = "cloudprovider"
    }

  def toInstance(db: CloudProviderDB): CloudProviderInstance = {
    val oeq  = db.entity
    val data = db.data
    CloudProviderInstance(
      id = oeq.uuid.id,
      name = oeq.name,
      description = oeq.description,
      baseUrl = data.baseUrl,
      iconUrl = data.iconUrl,
      providerAuth = data.providerAuth,
      oeqAuth = data.oeqAuth,
      serviceUris = data.serviceUris,
      viewers = data.viewers
    )
  }

  case class ProviderStdEdits(reg: CloudProviderRegistration) extends OEQEntityEdits {
    override def name               = reg.name
    override def nameStrings        = None
    override def description        = reg.description
    override def descriptionStrings = None
  }

  def validateRegistrationFields(oeq: OEQEntity,
                                 reg: CloudProviderRegistration,
                                 oeqAuth: CloudOAuthCredentials,
                                 locale: Locale): CloudProviderVal[CloudProviderDB] = {
    EntityValidation.standardValidation(ProviderStdEdits(reg), oeq, locale).map { newOeq =>
      val data = CloudProviderData(
        baseUrl = reg.baseUrl,
        iconUrl = reg.iconUrl,
        providerAuth = reg.providerAuth,
        oeqAuth = oeqAuth,
        serviceUris = reg.serviceUris,
        viewers = reg.viewers
      )
      CloudProviderDB(newOeq, data)
    }
  }

  def validToken(regToken: String): DB[CloudProviderVal[Unit]] = {
    LiftIO[DB].liftIO(IO {
      if (!tokenCache.get(regToken).isPresent)
        EntityValidation("token", "invalid").invalidNec
      else {
        tokenCache.invalidate(regToken)
        ().validNec
      }
    })
  }

  def register(
      regToken: String,
      registration: CloudProviderRegistration): DB[CloudProviderVal[CloudProviderInstance]] =
    validToken(regToken).flatMap {
      case Valid(_) =>
        for {
          oeq    <- EntityDB.newEntity(UUID.randomUUID())
          locale <- getContext.map(_.locale)
          validated = validateRegistrationFields(oeq,
                                                 registration,
                                                 CloudOAuthCredentials.random(),
                                                 locale)
          _ <- validated.traverse(cdb => flushDB(EntityDB.create(cdb)))
        } yield validated.map(toInstance)
      case Invalid(e) => e.invalid[CloudProviderInstance].pure[DB]
    }

  def editRegistered(id: UUID, registration: CloudProviderRegistration)
    : OptionT[DB, CloudProviderVal[CloudProviderInstance]] =
    EntityDB.readOne(id).semiflatMap { oeq =>
      for {
        locale <- getContext.map(_.locale)
        validated = validateRegistrationFields(oeq.entity, registration, oeq.data.oeqAuth, locale)
        _ <- validated.traverse(cdb => flushDB(EntityDB.update[CloudProviderDB](oeq.entity, cdb)))
      } yield validated.map(toInstance)
    }

  val createRegistrationToken: DB[String] = {
    LiftIO[DB].liftIO(IO {
      val newToken = UUID.randomUUID().toString
      tokenCache.put(newToken, newToken)
      newToken
    })
  }

  val readAll: Stream[DB, CloudProviderInstance] = {
    EntityDB.readAll[CloudProviderDB].map(toInstance)
  }

  val allProviders: Stream[DB, CloudProviderDetails] = {
    EntityDB.readAll[CloudProviderDB].map { cp =>
      val oeq = cp.entity
      CloudProviderDetails(id = oeq.uuid.id,
                           name = oeq.name,
                           description = oeq.description,
                           cp.data.iconUrl)
    }
  }
  def editFields(
      original: CloudProviderDB,
      edits: CloudProviderEditableDetails,
      locale: Locale
  ): CloudProviderVal[CloudProviderDB] =
    EntityValidation
      .cloudProviderValidation(
        new OEQEntityEdits() {
          override def name: String                = edits.name
          override def nameStrings                 = None
          override def description: Option[String] = edits.description
          override def descriptionStrings          = None
        },
        original.entity,
        locale
      )
      .map { newoeq =>
        CloudProviderDB(newoeq, original.data.copy(iconUrl = edits.iconUrl))
      }

  def editCloudProvider(id: UUID,
                        edits: CloudProviderEditableDetails): DB[CloudProviderVal[Boolean]] =
    getContext.map(_.locale).flatMap { locale =>
      EntityDB
        .readOne[CloudProviderDB](id)
        .semiflatMap { orig =>
          editFields(orig, edits, locale).traverse { edited =>
            flushDB(EntityDB.update[CloudProviderDB](orig.entity, edited)).as(true)
          }
        }
        .getOrElse(false.valid)
    }

  def deleteRegistration(id: UUID): DB[Unit] =
    EntityDB.delete(id).compile.drain

  def get(id: UUID): OptionT[DB, CloudProviderInstance] = {
    EntityDB.readOne(id).map(toInstance)
  }
}
