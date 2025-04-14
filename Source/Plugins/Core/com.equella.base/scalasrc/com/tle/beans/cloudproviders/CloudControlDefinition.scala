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

package com.tle.beans.cloudproviders

import java.util.{Optional, UUID}

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import scala.jdk.CollectionConverters._

case class CloudConfigOption(name: String, value: String)

object CloudControlConfigType extends Enumeration {
  val XPath, Textfield, Dropdown, Check, Radio = Value
}

case class CloudControlConfig(
    id: String,
    name: String,
    description: Option[String],
    configType: CloudControlConfigType.Value,
    options: Option[Iterable[CloudConfigOption]],
    min: Option[Int],
    max: Option[Int]
) {
  def isConfigMandatory: Boolean = {
    getMin > 0
  }

  def getDescription = Optional.ofNullable(description.orNull)
  def getOptions     = options.getOrElse(Iterable.empty).asJava
  def getMin         = min.getOrElse(0)
  def getMax         = max.getOrElse(Int.MaxValue)
}

case class CloudControlDefinition(
    providerId: UUID,
    controlId: String,
    name: String,
    iconUrl: String,
    configDefinition: Iterable[CloudControlConfig]
) {
  def getConfigDefinition = configDefinition.asJava
}

case class ProviderControlDefinition(
    name: String,
    iconUrl: Option[String],
    configuration: Iterable[CloudControlConfig]
)

object ProviderControlDefinition {
  implicit val typeDec        = Decoder.decodeEnumeration(CloudControlConfigType)
  implicit val ccoDec         = deriveDecoder[CloudConfigOption]
  implicit val dec            = deriveDecoder[CloudControlConfig]
  implicit val decodeControls = deriveDecoder[ProviderControlDefinition]
}
