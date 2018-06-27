package com.tle.core.db.tables

import java.time.Instant

import com.tle.core.db.types.{DbUUID, InstId}

case class ItemViewCount(inst: InstId, item_uuid: DbUUID, item_version: Int, count: Int, last_viewed: Instant)

case class AttachmentViewCount(inst: InstId, item_uuid: DbUUID, item_version: Int, attachment: DbUUID, count: Int, last_viewed: Instant)