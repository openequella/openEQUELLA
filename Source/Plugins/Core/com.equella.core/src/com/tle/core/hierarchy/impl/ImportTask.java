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

package com.tle.core.hierarchy.impl;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.EntityScript;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.hierarchy.ExportedHierarchyNode;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.security.TLEAclManager;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.common.usermanagement.user.UserState;

public class ImportTask extends SingleShotTask
{
	private final UserState userState;
	private final String xml;
	private final long inTo;
	private final boolean newids;
	private final boolean useSecurity;

	@Inject
	private HierarchyServiceImpl hierarchyService;
	@Inject
	private HierarchyDao dao;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private TLEAclManager aclManager;

	@Inject
	public ImportTask(@Assisted UserState userState, @Assisted String xml, @Assisted long inTo,
		@Assisted("newids") boolean newids, @Assisted("useSecurity") boolean useSecurity)
	{
		this.userState = userState;
		this.xml = xml;
		this.inTo = inTo;
		this.newids = newids;
		this.useSecurity = useSecurity;
	}

	@Override
	public Priority getPriority()
	{
		return Priority.INTERACTIVE;
	}

	@Override
	public void runTask() throws Exception
	{
		runAs.execute(userState, new Callable<Void>()
		{
			@Override
			public Void call() throws Exception
			{
				doImport();
				return null;
			}
		});
	}

	@Transactional
	void doImport()
	{
		HierarchyTopic parent = inTo <= 0 ? null : hierarchyService.getHierarchyTopic(inTo);
		ExportedHierarchyNode node = (ExportedHierarchyNode) hierarchyService.getXStream().fromXML(xml);
		initialiserService.initialiseClones(node);

		setupStatus(null, countTopics(node));

		doImportRecursive(node, parent);
	}

	private int countTopics(ExportedHierarchyNode node)
	{
		int count = 1;
		List<ExportedHierarchyNode> cs = node.getChildren();
		for( ExportedHierarchyNode c : cs )
		{
			count += countTopics(c);
		}
		return count;
	}

	/**
	 * @return true if the recursion should continue
	 */
	@SuppressWarnings("nls")
	private boolean doImportRecursive(ExportedHierarchyNode node, HierarchyTopic parent)
	{
		HierarchyTopic topic = node.getTopic();
		topic.setId(0l);
		if( newids )
		{
			topic.setUuid(UUID.randomUUID().toString());
		}
		else
		{
			HierarchyTopic previous = hierarchyService.getHierarchyTopicByUuid(topic.getUuid());
			if( previous != null )
			{
				addLogEntry(new ValidationError("uuid", CurrentLocale.get(previous.getName())));
				return false;
			}
		}

		processImportedQueries(topic.getAdditionalItemDefs());
		processImportedQueries(topic.getAdditionalSchemas());
		processImportedQueries(topic.getInheritedItemDefs());
		processImportedQueries(topic.getInheritedSchemas());

		hierarchyService.insert(topic, parent, Integer.MAX_VALUE);
		TargetList targetList = null;
		if( useSecurity )
		{
			targetList = node.getTargetList();
		}
		dao.flush();
		dao.clear();
		dao.evict(topic);

		aclManager.setTargetList(Node.HIERARCHY_TOPIC, topic, targetList);

		incrementWork();

		List<ExportedHierarchyNode> children = node.getChildren();
		if( children != null )
		{
			for( ExportedHierarchyNode childNode : children )
			{
				if( !doImportRecursive(childNode, topic) )
				{
					return false;
				}
			}
		}

		return true;
	}

	private <T extends BaseEntity> void processImportedQueries(List<? extends EntityScript<T>> queries)
	{
		if( queries != null )
		{
			for( Iterator<? extends EntityScript<T>> i = queries.iterator(); i.hasNext(); )
			{
				if( i.next().getEntity() == null )
				{
					i.remove();
				}
			}
		}
	}

	@Override
	protected String getTitleKey()
	{
		return null;
	}
}
