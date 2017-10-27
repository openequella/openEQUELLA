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

package com.tle.core.workflow.convert;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.tle.beans.TaskHistory;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.workflow.Workflow;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.XmlHelper;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.item.convert.ItemConverter.ItemExtrasConverter;
import com.tle.core.item.convert.WorkflowNodeConverter;
import com.tle.core.item.convert.WorkflowNodeConverter.WorkflowNodeSupplier;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.dao.TaskHistoryDao;
import com.tle.core.workflow.dao.WorkflowDao;
import com.tle.core.workflow.migrate.TaskHistoryMigrator;
import com.tle.core.xml.service.impl.XmlServiceImpl;

@Bind
@Singleton
public class TaskHistoryConverter implements ItemExtrasConverter
{
	public static final String TASKHISTORY_XML = "taskhistory.xml"; //$NON-NLS-1$

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private TaskHistoryDao taskHistoryDao;
	@Inject
	private XmlHelper xmlHelper;
	@Inject
	private WorkflowDao workflowDao;

	private XStream xstream;

	@Override
	public void importExtras(ItemConverterInfo info, XStream xstreamUnsed, SubTemporaryFile extrasFolder)
	{
		LoadingCache<String, Map<String, WorkflowNode>> workflowMaps = info.getState(TaskHistoryConverter.class);
		if( workflowMaps == null )
		{
			workflowMaps = CacheBuilder.newBuilder().build(new CacheLoader<String, Map<String, WorkflowNode>>()
			{
				@Override
				public Map<String, WorkflowNode> load(String input)
				{
					Workflow workflow = workflowDao.getByUuid(input);
					return workflow.getAllNodesAsMap();
				}
			});
			info.setState(TaskHistoryConverter.class, workflowMaps);
		}

		final LoadingCache<String, Map<String, WorkflowNode>> workflowMappings = workflowMaps;
		List<TaskHistory> taskHistories = info.getItemAttribute(TaskHistoryMigrator.class);
		if( !Check.isEmpty(taskHistories) )
		{
			saveTaskHistories(info, taskHistories);
		}
		else
		{
			try
			{
				if( fileSystemService.fileExists(extrasFolder, TASKHISTORY_XML) )
				{
					DataHolder data = getXStream().newDataHolder();
					data.put(WorkflowNodeSupplier.class, new WorkflowNodeSupplier()
					{
						@Override
						public long getIdForNode(String workflowUuid, String uuid)
						{
							Map<String, WorkflowNode> unchecked = workflowMappings.getUnchecked(workflowUuid);
							WorkflowNode workflowNode = unchecked.get(uuid);
							long id = workflowNode.getId();
							return id;
						}
					});

					taskHistories = xmlHelper.readXmlFile(extrasFolder, TASKHISTORY_XML, getXStream(), null, data);
					saveTaskHistories(info, taskHistories);
				}
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
	}

	private synchronized XStream getXStream()
	{
		if( xstream == null )
		{
			xstream = new XmlServiceImpl.ExtXStream(getClass().getClassLoader()) {
				@Override
				protected MapperWrapper wrapMapper(MapperWrapper next) {
					return new HibernateMapper(next);
				}
			};
			xstream.registerConverter(new WorkflowNodeConverter());
		}
		return xstream;
	}

	private void saveTaskHistories(ItemConverterInfo info, List<TaskHistory> taskHistories)
	{
		for( TaskHistory th : taskHistories )
		{
			th.setItem(info.getItem());
			th.setId(0);
			taskHistoryDao.save(th);
		}
	}

	@Override
	public void exportExtras(ItemConverterInfo info, XStream xstreamUnused, SubTemporaryFile extrasFolder)
	{
		List<TaskHistory> taskHistories = taskHistoryDao.getAllTasksForItem(info.getItem());

		if( !taskHistories.isEmpty() )
		{
			xmlHelper.writeXmlFile(extrasFolder, TASKHISTORY_XML, taskHistories, getXStream());
		}
	}
}
