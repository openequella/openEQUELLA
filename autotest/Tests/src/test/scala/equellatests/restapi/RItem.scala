package equellatests.restapi

import java.util.UUID

import equellatests.restapi.RHistoryEventType.RHistoryEventType
import equellatests.restapi.RNodeStatus.RNodeStatus
import equellatests.restapi.RStatusNodeType.RStatusNodeType
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.auto._

object RStatus extends Enumeration {
  type RStatus = Value
  val live, moderating, rejected, draft     = Value
  implicit val encRStatus: Encoder[RStatus] = Encoder.encodeEnumeration(RStatus)
  implicit val decRStatus: Decoder[RStatus] = Decoder.decodeEnumeration(RStatus)
}

import RStatus._

case class RCollectionRef(uuid: UUID)

case class RUserRef(id: String)

case class RUserRefO(id: Option[String])

case class RCreateItem(
    collection: RCollectionRef,
    metadata: String,
    attachments: Seq[BasicAttachment] = Seq.empty
)

case class RItem(
    uuid: UUID,
    version: Int,
    collection: RCollectionRef,
    metadata: String,
    name: Option[String],
    status: RStatus,
    attachments: Seq[BasicAttachment]
)

sealed trait RAttachment

case class BasicAttachment(
    description: String,
    viewer: Option[String],
    restricted: Boolean,
    thumbnail: Option[String]
) extends RAttachment {
  def viewerO: Option[String] = viewer.filterNot(_.isEmpty)
}

object RNodeStatus extends Enumeration {
  type RNodeStatus = Value
  val incomplete, complete                      = Value
  implicit val encRStatus: Encoder[RNodeStatus] = Encoder.encodeEnumeration(RNodeStatus)
  implicit val decRStatus: Decoder[RNodeStatus] = Decoder.decodeEnumeration(RNodeStatus)
}

object RStatusNodeType extends Enumeration {
  type RStatusNodeType = Value
  val serial, task                                  = Value
  implicit val encRStatus: Encoder[RStatusNodeType] = Encoder.encodeEnumeration(RStatusNodeType)
  implicit val decRStatus: Decoder[RStatusNodeType] = Decoder.decodeEnumeration(RStatusNodeType)
}

sealed trait StatusNode {
  def children: Seq[StatusNode]
}

case class StandardStatus(
    uuid: UUID,
    name: String,
    status: RNodeStatus,
    children: Seq[StatusNode],
    `type`: RStatusNodeType
) extends StatusNode

case class TaskStatus(
    uuid: UUID,
    name: String,
    status: RNodeStatus,
    children: Seq[StatusNode],
    assignedTo: RUserRefO
) extends StatusNode

object StatusNode {

  implicit val decStatusNode: Decoder[StatusNode] = new Decoder[StatusNode] {
    final def apply(c: HCursor): Decoder.Result[StatusNode] =
      c.downField("type").as[String].flatMap { typ =>
        typ match {
          case "task" => c.as[TaskStatus]
          case o      => c.as[StandardStatus]
        }
      }
  }
}

case class RModeration(status: RStatus, nodes: Option[StatusNode]) {
  def allNodes: Seq[StatusNode] = {
    def plusChildren(node: StatusNode): Seq[StatusNode] = {
      Seq(node) ++ node.children.flatMap(plusChildren)
    }

    nodes.map(plusChildren).getOrElse(Seq.empty)
  }

  def firstIncompleteTask: Option[TaskStatus] = allNodes.collectFirst {
    case ts @ TaskStatus(_, _, RNodeStatus.incomplete, _, _) => ts
  }
}

object RHistoryEventType extends Enumeration {
  type RHistoryEventType = Value
  val taskMove, edit, statechange, resetworkflow     = Value
  implicit val enc: Encoder[RHistoryEventType.Value] = Encoder.encodeEnumeration(RHistoryEventType)
  implicit val dec: Decoder[RHistoryEventType.Value] = Decoder.decodeEnumeration(RHistoryEventType)
}

case class RHistoryEvent(
    `type`: RHistoryEventType,
    user: RUserRef,
    state: RStatus,
    step: Option[UUID],
    stepName: Option[String],
    comment: Option[String],
    toStep: Option[UUID],
    toStepName: Option[String]
)
