package com.tle.web.api.search

import com.tle.common.searching.SortField
import com.tle.common.searching.SortField.Type
import com.tle.core.workflow.freetext.TasksIndexer

/**
  * Represents the `order` values which can be used for sorting specific to items under moderation -
  * which are stored in the 'task' index (hence the name).
  */
object TaskSortOrder {
  val ID_LASTACTION: String = "task_lastaction"
  val ID_SUBMITTED: String  = "task_submitted"

  /**
    * Give an ID of an expected Task sort order, will create a `SortField` that can be used with
    * DefaultSearch to achieve that order. However if not one of the Task orders, then will simply
    * return None.
    *
    * @param id a possible Task sort order specifier
    * @return a `SortField` representing the specified order, or None if the provided ID is unknown
    */
  def apply(id: String): Option[SortField] = id match {
    case ID_LASTACTION => Some(new SortField(TasksIndexer.FIELD_LASTACTION, false, Type.LONG))
    case ID_SUBMITTED  => Some(new SortField(TasksIndexer.FIELD_STARTWORKFLOW, true, Type.LONG))
    case _             => None
  }
}
