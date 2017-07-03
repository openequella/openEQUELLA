package com.tle.core.workflow.service;

import java.util.Date;
import java.util.List;

import com.tle.beans.item.Item;
import com.tle.common.workflow.Trend;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.core.workflow.TaskTrend;

public interface TaskStatisticsService
{
	List<TaskTrend> getWaitingTasks(Trend trend);

	List<TaskTrend> getWaitingTasksForWorkflow(String uuid, Trend trend);

	void enterTask(Item item, WorkflowItem task, Date entry);

	void exitTask(Item item, WorkflowItem task, Date entry);

	void exitAllTasksForItem(Item item, Date end);

	void restoreTasksForItem(Item item);
}