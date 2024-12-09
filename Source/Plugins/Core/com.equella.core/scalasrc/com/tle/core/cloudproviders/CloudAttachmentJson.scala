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

import java.util.UUID

import com.tle.beans.item.attachments.CustomAttachment
import io.circe.Json
import io.circe.generic.semiauto._
import io.circe.parser._

case class CloudAttachmentJson(
    providerId: UUID,
    display: Option[Map[String, Json]],
    meta: Option[Map[String, Json]],
    indexText: Option[String],
    indexFiles: Option[Iterable[String]]
)

object CloudAttachmentJson {
  val defaultJson = CloudAttachmentJson(new UUID(0L, 0L), None, None, None, None)

  final val JsonField = "json"
  val encode          = deriveEncoder[CloudAttachmentJson]
  val decode          = deriveDecoder[CloudAttachmentJson]

  def decodeJson(attachment: CustomAttachment): CloudAttachmentJson = {
    parse(attachment.getData(JsonField).asInstanceOf[String])
      .flatMap(CloudAttachmentJson.decode.decodeJson)
      .getOrElse(defaultJson)
  }

  def encodeJson(attachment: CustomAttachment, data: CloudAttachmentJson): Unit = {
    attachment.setData(JsonField, encode(data).noSpaces)
  }
}
