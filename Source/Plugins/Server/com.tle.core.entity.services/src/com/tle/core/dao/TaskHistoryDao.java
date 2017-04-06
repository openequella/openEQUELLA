package com.tle.core.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.tle.beans.TaskHistory;
import com.tle.beans.item.Item;
import com.tle.core.hibernate.dao.GenericDao;
import com.tle.core.services.TaskTrend;

public interface TaskHistoryDao extends GenericDao<TaskHistory, Long>
{
	void exitAllTasksForItem(Item item, Date end);

	void restoreTasksForItem(Item item);

	List<TaskTrend> getTaskTrendsForWorkflows(Collection<String> uuid, Date date);

	List<TaskHistory> getAllTasksForItem(Item item);
}
