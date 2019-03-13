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

import java.util.{Locale, UUID}

import cats.data.ValidatedNec
import com.tle.core.db._
import com.tle.core.db.dao.{EntityDB, EntityDBExt}
import com.tle.core.db.tables.OEQEntity
import com.tle.core.validation.EntityValidation
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

  def validateRegistrationFields(oeq: OEQEntity,
                                 reg: CloudProviderRegistration,
                                 oeqAuth: CloudOAuthCredentials,
                                 locale: Locale): CloudProviderVal[CloudProviderDB] = {
    EntityValidation.nonBlank("name", reg.name, locale).map { name =>
      val newOeq = oeq.copy(name = name._1, name_strings = name._2)
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

  def register(
      registration: CloudProviderRegistration): DB[CloudProviderVal[CloudProviderInstance]] =
    for {
      oeq    <- EntityDB.newEntity(UUID.randomUUID())
      locale <- getContext.map(_.locale)
      validated = validateRegistrationFields(oeq,
                                             registration,
                                             CloudOAuthCredentials.random(),
                                             locale)
      _ <- validated.traverse(cdb => flushDB(EntityDB.create(cdb)))
    } yield validated.map(toInstance)

}
