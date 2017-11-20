/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.workflow.dao.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.beans.TaskHistory;
import com.tle.beans.item.Item;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.item.dao.ItemDaoExtension;
import com.tle.core.workflow.TaskTrend;
import com.tle.core.workflow.dao.TaskHistoryDao;
import com.tle.core.workflow.dao.WorkflowDao;
import com.tle.core.workflow.event.WorkflowChangeEvent;
import com.tle.core.workflow.event.listener.WorkflowChangeListener;

@Bind(TaskHistoryDao.class)
@Singleton
@SuppressWarnings("nls")
public class TaskHistoryDaoImpl extends GenericDaoImpl<TaskHistory, Long>
	implements
		TaskHistoryDao,
		ItemDaoExtension,
		WorkflowChangeListener
{
	@Inject
	private WorkflowDao workflowDao;

	public TaskHistoryDaoImpl()
	{
		super(TaskHistory.class);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void exitAllTasksForItem(final Item item, final Date end)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session
					.createQuery("UPDATE TaskHistory SET exitDate = :date WHERE item = :item AND exitDate IS NULL");
				query.setParameter("date", end);
				query.setParameter("item", item);

				return query.executeUpdate();
			}
		});
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void restoreTasksForItem(Item item)
	{
		List<WorkflowItem> incompleteTasks = workflowDao.getIncompleteTasks(item);
		for( WorkflowItem task : incompleteTasks )
		{
			save(new TaskHistory(item, task, new Date(), null));
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void delete(final Item item)
	{
		getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("DELETE FROM TaskHistory WHERE item = :item");
				query.setParameter("item", item);

				return query.executeUpdate();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<TaskTrend> getTaskTrendsForWorkflows(final Collection<String> uuids, final Date date)
	{
		if( uuids.isEmpty() )
		{
			return Collections.emptyList();
		}

		List<Object[]> results = getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(final Session session)
			{
				final Query query = session
					.createQuery("SELECT wi.id, wi.name.id, count(*) FROM TaskHistory th JOIN th.task wi "
						+ "WHERE wi.workflow.institution = :inst and wi.id = th.task.id AND wi.workflow.uuid in (:uuids) AND th.exitDate IS NULL "
						+ "GROUP BY wi.id, wi.name.id ORDER BY count(*) DESC");
				query.setParameter("inst", CurrentInstitution.get());
				query.setParameterList("uuids", uuids);
				return query.setMaxResults(5).list();
			}
		});
		List<TaskTrend> trendList = Lists.newArrayList();
		final Map<Long, TaskTrend> trendMap = Maps.newHashMap();
		for( Object[] objects : results )
		{
			TaskTrend trend = new TaskTrend(((Number) objects[0]).longValue(), ((Number) objects[1]).longValue(),
				((Number) objects[2]).intValue());
			trendList.add(trend);
			trendMap.put(trend.getWorkflowItemId(), trend);
		}
		if( trendList.isEmpty() )
		{
			return trendList;
		}

		List<Object[]> trendResults = getHibernateTemplate().executeFind(new HibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session)
			{
				Query query = session
					.createQuery("SELECT wi.id, count(*) FROM TaskHistory th JOIN th.task wi WHERE wi.id IN (:ids) AND "
						+ "th.entryDate < :date AND (th.exitDate > :date OR th.exitDate IS NULL)"
						+ "GROUP BY wi.id, wi.name.id ORDER BY count(*) DESC");
				query.setParameterList("ids", trendMap.keySet());
				query.setParameter("date", date);
				return query.list();
			}
		});
		for( Object[] objects : trendResults )
		{
			long workflowItemId = ((Number) objects[0]).longValue();
			TaskTrend trend = trendMap.get(workflowItemId);
			trend.setTrend(trend.getWaiting() - ((Number) objects[1]).intValue());
		}

		return trendList;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<TaskHistory> getAllTasksForItem(Item item)
	{
		return findAllByCriteria(Restrictions.eq("item.id", item.getId()));
	}

	@Override
	public void workflowChange(WorkflowChangeEvent event)
	{
		if( event.isDelete() )
		{
			final long workflowId = event.getWorkflowId();
			final Set<WorkflowNode> nodes = event.getNodes();
			if( nodes == null )
			{
				getHibernateTemplate().execute(new HibernateCallback()
				{
					@Override
					public Object doInHibernate(Session session)
					{
						Query query = session.createQuery("DELETE FROM TaskHistory th WHERE th.task in "
							+ "(select wn.id from Workflow w join w.nodes as wn where w.id = :workflow)");
						query.setParameter("workflow", workflowId);
						return query.executeUpdate();
					}
				});
			}
			else
			{
				getHibernateTemplate().execute(new HibernateCallback()
				{
					@Override
					public Object doInHibernate(Session session)
					{
						Query query = session.createQuery("DELETE FROM TaskHistory th WHERE th.task in (:nodes)");
						query.setParameterList("nodes", nodes);
						return query.executeUpdate();
					}
				});
			}
		}

	}
}
