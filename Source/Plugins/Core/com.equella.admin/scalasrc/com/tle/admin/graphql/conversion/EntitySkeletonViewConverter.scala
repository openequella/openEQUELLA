package com.tle.admin.graphql.conversion

import com.tle.beans.entity.BaseEntity
import com.tle.common.EntityPack
import io.github.openequella.graphql.api.views.EntitySkeletonView

object EntitySkeletonViewConverter {
  def toEntityPack[T <: BaseEntity](emptyEntity: T)(view: EntitySkeletonView): EntityPack[T] = {
    emptyEntity.setOwner(view.owner)
    emptyEntity.setUuid(view.uuid)

    new EntityPack[T](emptyEntity, view.stagingId)
  }
}
