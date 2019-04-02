package com.tle.core.db.tables

import java.time.Instant

import com.tle.core.db.types.{InstId, String255}

case class CachedValue(id: Long,
                       cache_id: String255,
                       key: String255,
                       ttl: Option[Instant],
                       value: String,
                       institution_id: InstId)
