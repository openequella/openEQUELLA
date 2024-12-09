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

import java.util
import java.util.UUID
import com.google.common.collect.ImmutableMap
import com.tle.beans.item.attachments.{Attachment, CustomAttachment}
import com.tle.core.item.edit.ItemEditor
import com.tle.core.item.serializer.AbstractAttachmentSerializer
import com.tle.web.api.item.equella.interfaces.beans.{CloudAttachmentBean, EquellaAttachmentBean}
import io.circe.Json.Folder
import io.circe.syntax._
import io.circe.{Json, JsonNumber, JsonObject}
import scala.jdk.CollectionConverters._

class CloudAttachmentSerializer extends AbstractAttachmentSerializer {
  import CloudAttachmentSerializer._
  override def serialize(attachment: Attachment): EquellaAttachmentBean = attachment match {
    case a: CustomAttachment =>
      val cab    = new CloudAttachmentBean()
      val fields = CloudAttachmentFields(a)
      cab.setCloudType(fields.cloudType)
      cab.setVendorId(fields.vendorId)
      cab.setProviderId(fields.providerId)
      val cloudJson = fields.cloudJson
      cab.setDisplay(toJavaMap(cloudJson.display))
      cab.setMeta(toJavaMap(cloudJson.meta))
      cab.setIndexText(cloudJson.indexText.orNull)
      cab.setIndexFiles(cloudJson.indexFiles.map(_.asJavaCollection).orNull)
      cab
  }

  override def deserialize(bean: EquellaAttachmentBean, itemEditor: ItemEditor): String =
    bean match {
      case cloudBean: CloudAttachmentBean =>
        val uuid   = bean.getUuid
        val editor = itemEditor.getAttachmentEditor(uuid, classOf[CloudAttachmentEditor])
        editStandard(editor, cloudBean)
        editor.editProviderId(cloudBean.getProviderId)
        editor.editVendorId(cloudBean.getVendorId)
        editor.editCloudType(cloudBean.getCloudType)
        editor.editDisplay(toScalaMap(cloudBean.getDisplay))
        editor.editMeta(toScalaMap(cloudBean.getMeta))
        editor.editIndexText(Option(cloudBean.getIndexText))
        editor.editIndexFiles(Option(cloudBean.getIndexFiles).map(_.asScala))
        editor.finish()
        editor.getAttachmentUuid
    }

  def toJavaMap(smap: Option[Map[String, Json]]): java.util.Map[String, Object] =
    smap.map(_.view.mapValues(_.foldWith(javaFolder)).toMap.asJava).orNull

  def toScalaMap(jmap: java.util.Map[String, Object]): Option[Map[String, Json]] =
    Option(jmap).map(_.asScala.view.mapValues(fromJava).toMap)

  override def getAttachmentBeanTypes: util.Map[String, Class[_ <: EquellaAttachmentBean]] =
    ImmutableMap.of("cloud", classOf[CloudAttachmentBean])

  override def exportable(bean: EquellaAttachmentBean): Boolean = false

}

case class CloudAttachmentFields(attachment: CustomAttachment) {
  val cloudJson         = CloudAttachmentJson.decodeJson(attachment)
  def cloudType: String = attachment.getValue2
  def vendorId: String  = attachment.getValue3
  def providerId: UUID  = cloudJson.providerId
}

object CloudAttachmentSerializer {
  val javaFolder = new Folder[Object] {
    override def onNull: AnyRef = null

    override def onBoolean(value: Boolean): AnyRef = value.asInstanceOf[AnyRef]

    override def onNumber(value: JsonNumber): AnyRef =
      value.toLong
        .map(_.asInstanceOf[java.lang.Long])
        .getOrElse(value.toDouble.asInstanceOf[java.lang.Double])

    override def onString(value: String): AnyRef = value

    override def onArray(value: Vector[Json]): AnyRef = value.map(_.foldWith(this)).asJava

    override def onObject(value: JsonObject): AnyRef =
      value.toMap.view.mapValues(_.foldWith(this)).asJava
  }

  def fromJava(obj: Any): Json = obj match {
    case s: String                  => s.asJson
    case n: java.lang.Long          => n.asJson
    case i: java.lang.Integer       => i.asJson
    case b: java.lang.Boolean       => b.asJson
    case a: java.util.Collection[_] => a.asScala.map(fromJava).asJson
    case m: java.util.Map[_, _] =>
      m.asScala.map { case (k, v) =>
        (k.toString, fromJava(v))
      }.asJson
  }

}
