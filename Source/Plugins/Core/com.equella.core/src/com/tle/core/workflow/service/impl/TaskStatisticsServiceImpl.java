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

package com.tle.core.workflow.service.impl;

import com.tle.beans.TaskHistory;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.Item;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.Trend;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.TaskTrend;
import com.tle.core.workflow.dao.TaskHistoryDao;
import com.tle.core.workflow.service.TaskStatisticsService;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.exceptions.AccessDeniedException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

@Bind(TaskStatisticsService.class)
@Singleton
@SuppressWarnings("nls")
public class TaskStatisticsServiceImpl implements TaskStatisticsService {
  @Inject private TaskHistoryDao taskHistoryDao;
  @Inject private WorkflowService workflowService;

  @Override
  public void enterTask(Item item, WorkflowItem task, Date entry) {
    TaskHistory old =
        taskHistoryDao.findByCriteria(
            Restrictions.eq("task.id", task.getId()),
            Restrictions.eq("item.id", item.getId()),
            Restrictions.isNull("exitDate"));
    if (old != null) {
      throw new Error(
          "Task History never exited for this task: "
              + CurrentLocale.get(task.getName())
              + "   ID="
              + task.getId());
    }
    taskHistoryDao.save(new TaskHistory(item, task, entry, null));
  }

  @Override
  @Transactional
  public void exitTask(Item item, WorkflowItem task, Date exit) {
    TaskHistory th =
        taskHistoryDao.findByCriteria(
            Restrictions.eq("task.id", task.getId()),
            Restrictions.eq("item.id", item.getId()),
            Restrictions.isNull("exitDate"));
    th.setExitDate(exit);
    taskHistoryDao.update(th);
  }

  @Override
  @Transactional
  public void exitAllTasksForItem(Item item, Date end) {
    if (item.isModerating()) {
      taskHistoryDao.exitAllTasksForItem(item, end);
    }
  }

  @Override
  @Transactional
  public void restoreTasksForItem(Item item) {
    taskHistoryDao.restoreTasksForItem(item);
  }

  @Override
  @Transactional
  public List<TaskTrend> getWaitingTasks(Trend trend) {
    return taskHistoryDao.getTaskTrendsForWorkflows(
        getManageableWorkflowUuids(), getTrendDate(trend));
  }

  @Override
  @Transactional
  public List<TaskTrend> getWaitingTasksForWorkflow(String uuid, Trend trend) {
    if (!getManageableWorkflowUuids().contains(uuid)) {
      throw new AccessDeniedException(
          CurrentLocale.get("com.tle.web.api.workflow.error.workflowAccessDenied", uuid));
    }

    return taskHistoryDao.getTaskTrendsForWorkflows(
        Collections.singleton(uuid), getTrendDate(trend));
  }

  private Set<String> getManageableWorkflowUuids() {
    return workflowService.listManageable().stream()
        .map(BaseEntityLabel::getUuid)
        .collect(Collectors.toSet());
  }

  private Date getTrendDate(Trend trend) {
    final Date now = new Date();
    return new Date(now.getTime() - TimeUnit.DAYS.toMillis(trend.getDays()));
  }
}
