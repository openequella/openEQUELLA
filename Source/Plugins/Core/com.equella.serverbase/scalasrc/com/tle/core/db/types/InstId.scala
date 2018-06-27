package com.tle.core.db.types

import com.tle.beans.Institution
import io.doolse.simpledba.Iso

case class InstId(id: Long) extends AnyVal

object InstId
{
  implicit def longIso = Iso[InstId, Long](_.id, InstId.apply)

  implicit def instToId(inst: Institution): InstId = InstId(inst.getDatabaseId)
}
