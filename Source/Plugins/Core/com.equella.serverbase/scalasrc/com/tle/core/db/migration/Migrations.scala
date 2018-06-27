package com.tle.core.db.migration

import java.util
import java.util.{Calendar, Date}

import com.tle.core.i18n.ServerStrings
import com.tle.core.migration.MigrationExt

import scala.collection.JavaConverters._

object Migrations {

  def migrationList : util.Collection[MigrationExt] = Iterable[MigrationExt](NewAuditLogColumn, NewViewCountTables).asJavaCollection
}
