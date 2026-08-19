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

package io.github.openequella.graphql.client

import caliban.client.CalibanClientError.DecodingError
import caliban.client._
import caliban.client.__Value._

sealed trait OtherTargetListType extends scala.Product with scala.Serializable { def value: String }
object OtherTargetListType {
  case object ITEM_METADATA extends OtherTargetListType { val value: String = "ITEM_METADATA" }
  case object ITEM_STATUS   extends OtherTargetListType { val value: String = "ITEM_STATUS"   }
  case object WORKFLOW_TASK extends OtherTargetListType { val value: String = "WORKFLOW_TASK" }

  implicit val decoder: ScalarDecoder[OtherTargetListType] = {
    case __StringValue("ITEM_METADATA") => Right(OtherTargetListType.ITEM_METADATA)
    case __StringValue("ITEM_STATUS")   => Right(OtherTargetListType.ITEM_STATUS)
    case __StringValue("WORKFLOW_TASK") => Right(OtherTargetListType.WORKFLOW_TASK)
    case other => Left(DecodingError(s"Can't build OtherTargetListType from input $other"))
  }
  implicit val encoder: ArgEncoder[OtherTargetListType] = {
    case OtherTargetListType.ITEM_METADATA => __EnumValue("ITEM_METADATA")
    case OtherTargetListType.ITEM_STATUS   => __EnumValue("ITEM_STATUS")
    case OtherTargetListType.WORKFLOW_TASK => __EnumValue("WORKFLOW_TASK")
  }

  val values: scala.collection.immutable.Vector[OtherTargetListType] =
    scala.collection.immutable.Vector(ITEM_METADATA, ITEM_STATUS, WORKFLOW_TASK)
}
