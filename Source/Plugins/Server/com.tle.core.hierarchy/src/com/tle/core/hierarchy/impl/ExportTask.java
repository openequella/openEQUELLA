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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.hierarchy.ExportedHierarchyNode;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Item;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.hibernate.equella.service.InitialiserCallback;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.hibernate.equella.service.Property;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.hierarchy.HierarchyService;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.impl.SingleShotTask;

@SuppressWarnings("nls")
public class ExportTask extends SingleShotTask
{
	private final UserState userState;
	private final long exportId;
	private final boolean withSecurity;

	@Inject
	private HierarchyService hierarchyService;
	@Inject
	private HierarchyDao dao;
	@Inject
	private RunAsInstitution runAs;
	@Inject
	private InitialiserService initialiserService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService instituionService;

	@Inject
	public ExportTask(@Assisted UserState userState, @Assisted long exportId, @Assisted boolean withSecurity)
	{
		this.userState = userState;
		this.exportId = exportId;
		this.withSecurity = withSecurity;
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
				doExport();
				return null;
			}
		});
	}

	@Transactional
	void doExport() throws IOException
	{
		final HierarchyTopic topic = hierarchyService.getHierarchyTopic(exportId);

		setupStatus(null, dao.countSubnodesForNode(topic));

		ExportedHierarchyNode nodes = populateExport(topic);
		nodes = initialiserService.initialise(nodes, new InitialiserCallback()
		{
			@Override
			public void set(Object obj, Property property, Object value)
			{
				if( value instanceof BaseEntity )
				{
					BaseEntity toset = (BaseEntity) value;
					toset.setUuid(((BaseEntity) property.get(obj)).getUuid());
				}
				property.set(obj, value);
			}

			@Override
			public void entitySimplified(Object old, Object newObj)
			{
				if( old instanceof BaseEntity )
				{
					BaseEntity toset = (BaseEntity) newObj;
					BaseEntity oldObj = (BaseEntity) old;
					toset.setUuid(oldObj.getUuid());
				}
				if( old instanceof Item )
				{
					Item item = (Item) old;
					Item newItem = (Item) newObj;
					newItem.setUuid(item.getUuid());
					newItem.setVersion(item.getVersion());
				}
			}
		});

		StagingFile staging = stagingService.createStagingArea();
		fileSystemService.write(staging, "topic.xml", new StringReader(hierarchyService.getXStream().toXML(nodes)),
			false);

		// Feels so dirty... Should probably be a method on
		// StagingService to get a URL. We do this exact same thing in
		// ServerBackendImpl for the file manager.
		addLogEntry(URLUtils.newURL(instituionService.getInstitutionUrl(),
			"file/" + staging.getUuid() + "/$/" + URLUtils.urlEncode("topic.xml")));
	}

	private ExportedHierarchyNode populateExport(HierarchyTopic topic)
	{
		ExportedHierarchyNode node = new ExportedHierarchyNode();
		node.setTopic(topic);

		if( withSecurity )
		{
			node.setTargetList(aclManager.getTargetList(Node.HIERARCHY_TOPIC, topic));
		}

		incrementWork();

		List<ExportedHierarchyNode> childNodes = new ArrayList<ExportedHierarchyNode>();
		for( HierarchyTopic childTopic : hierarchyService.getChildTopics(topic) )
		{
			childNodes.add(populateExport(childTopic));
		}
		node.setChildren(childNodes);

		return node;
	}

	@Override
	protected String getTitleKey()
	{
		return null;
	}
}
