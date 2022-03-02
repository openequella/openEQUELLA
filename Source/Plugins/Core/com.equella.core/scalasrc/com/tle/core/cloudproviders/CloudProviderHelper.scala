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

package com.tle.core.cloudproviders

import cats.data.ValidatedNec
import cats.implicits._
import com.tle.beans.newentity.Entity
import com.tle.core.validation.EntityValidation
import io.circe.parser.decode
import java.util.UUID

object CloudProviderHelper {

  /**
    * Transform an Entity's custom data from String to CloudProviderData.
    *
    * @param data String representing the custom data of an Entity in JSON format.
    * @return ValidatedNec where left is an EntityValidation explaining why the transformation failed
    *         and right is the transformed CloudProviderData.
    */
  def extractData(data: String): ValidatedNec[EntityValidation, CloudProviderData] =
    decode[CloudProviderData](data)
      .leftMap(err => EntityValidation("data", err.getMessage))
      .toValidatedNec

  /**
    * Build a CloudProviderInstance by Entity and CloudProviderData.
    *
    * @param entity The Entity where the type must be 'cloudprovider'.
    * @param data Custom data specific to Cloud provider.
    */
  def toInstance(entity: Entity, data: CloudProviderData): CloudProviderInstance = {
    CloudProviderInstance(
      id = UUID.fromString(entity.id.uuid),
      name = entity.name,
      description = Option(entity.description),
      vendorId = data.vendorId,
      baseUrl = data.baseUrl,
      iconUrl = data.iconUrl,
      providerAuth = data.providerAuth,
      oeqAuth = data.oeqAuth,
      serviceUrls = data.serviceUrls,
      viewers = data.viewers
    )
  }
}
