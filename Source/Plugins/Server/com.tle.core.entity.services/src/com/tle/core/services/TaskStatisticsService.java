package com.tle.core.services;

import java.util.Date;
import java.util.List;

import com.tle.beans.item.Item;
import com.tle.common.workflow.node.WorkflowItem;

public interface TaskStatisticsService
{
	public enum Trend
	{
		WEEK(7), MONTH(30);

		private final int days;

		Trend(int days)
		{
			this.days = days;
		}

		public int getDays()
		{
			return days;
		}
	}

	List<TaskTrend> getWaitingTasks(Trend trend);

	List<TaskTrend> getWaitingTasksForWorkflow(String uuid, Trend trend);

	void enterTask(Item item, WorkflowItem task, Date entry);

	void exitTask(Item item, WorkflowItem task, Date entry);

	void exitAllTasksForItem(Item item, Date end);

	void restoreTasksForItem(Item item);
}