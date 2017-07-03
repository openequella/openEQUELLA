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

package com.tle.core.harvester.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.java.plugin.registry.Extension;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.beans.exception.ValidationError;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.harvester.RemoteHarvesterProfileService;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.entity.EntityEditingBean;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.HarvesterProfileDao;
import com.tle.core.harvester.HarvesterProfileService;
import com.tle.core.harvester.old.ContentRepository;
import com.tle.core.institution.RunAsInstitution;
import com.tle.core.plugins.PluginService;
import com.tle.core.security.impl.SecureEntity;
import com.tle.core.services.TaskService;
import com.tle.core.services.impl.BeanClusteredTask;
import com.tle.core.services.impl.SingleShotTask;
import com.tle.core.services.impl.Task;

@SuppressWarnings("nls")
@Bind(HarvesterProfileService.class)
@Singleton
@SecureEntity(RemoteHarvesterProfileService.ENTITY_TYPE)
public class HarvesterProfileServiceImpl
	extends
		AbstractEntityServiceImpl<EntityEditingBean, HarvesterProfile, HarvesterProfileService>
	implements
		HarvesterProfileService
{
	private final HarvesterProfileDao harvesterProfileDao;

	@Inject
	private RunAsInstitution runAs;

	@Inject
	private TaskService taskService;

	@Inject
	private PluginService pluginService;

	@Inject
	public HarvesterProfileServiceImpl(HarvesterProfileDao harvesterProfileDao)
	{
		super(Node.HARVESTER_PROFILE, harvesterProfileDao);
		this.harvesterProfileDao = harvesterProfileDao;
	}

	@Override
	@Transactional
	public void updateLastRun(HarvesterProfile profile, Date lastRun)
	{
		harvesterProfileDao.updateLastRun(profile, lastRun);
	}

	@Override
	protected void doValidation(EntityEditingSession<EntityEditingBean, HarvesterProfile> session,
		HarvesterProfile entity, List<ValidationError> errors)
	{
		// Nothing to validate
	}

	@Override
	@Transactional
	public List<HarvesterProfile> enumerateEnabledProfiles()
	{
		return findAllWithCriterion(Restrictions.eq("enabled", true));
	}

	@Override
	public void startHarvesterTask(String uuid, boolean manualKickoff)
	{
		long instId = CurrentInstitution.get().getUniqueId();
		String taskName = "HarvesterTask-" + instId + uuid;
		BeanClusteredTask task = new BeanClusteredTask(taskName, HarvesterProfileService.class, "createHarvesterTask",
			uuid, instId, manualKickoff);
		taskService.getGlobalTask(task, TimeUnit.MINUTES.toMillis(1));
	}

	public Task createHarvesterTask(String harvesterUuid, long institutionId, boolean manualKickoff)
	{
		return new HarvesterTask(harvesterUuid, institutionId, manualKickoff);
	}

	public int runProfile(String profileUuid, boolean testOnly) throws Exception
	{
		HarvesterProfile profile = getForExecutre(profileUuid);
		final SortedMap<String, String> plugins = new TreeMap<String, String>();
		final Map<String, Extension> tools = new HashMap<String, Extension>();

		Collection<Extension> extensions = pluginService.getConnectedExtensions("com.tle.core.harvester",
			"harvesterProtocol");
		for( Extension extension : extensions )
		{
			String type = extension.getParameter("type").valueAsString();
			String name = extension.getParameter("name").valueAsString();
			plugins.put(name, type);
			tools.put(type, extension);
		}

		Extension extension = tools.get(profile.getType());

		ContentRepository contentRepository = (ContentRepository) pluginService
			.getBean(extension.getDeclaringPluginDescriptor(), extension.getParameter("class").valueAsString());

		return contentRepository.setupAndRun(profile, testOnly);
	}

	@Transactional
	protected HarvesterProfile getForExecutre(String profileUuid)
	{
		HarvesterProfile profile = getByUuid(profileUuid);
		profile.getAttributes();
		return profile;
	}

	public final class HarvesterTask extends SingleShotTask
	{
		private final String profileUuid;
		private final long institutionId;
		private final boolean manualKickoff;

		public HarvesterTask(String profileUuid, long institutionId, boolean manualKickoff)
		{
			this.profileUuid = profileUuid;
			this.institutionId = institutionId;
			this.manualKickoff = manualKickoff;
		}

		@Override
		protected String getTitleKey()
		{
			return "com.tle.core.harvester.task.title";
		}

		@Override
		public Priority getPriority()
		{
			return manualKickoff ? Priority.NORMAL : Priority.BACKGROUND;
		}

		@Override
		public void runTask() throws Exception
		{
			runAs.executeAsSystem(institutionService.getInstitution(institutionId), new Callable<Void>()
			{
				@Override
				public Void call() throws Exception
				{
					runProfile(profileUuid, false);
					return null;
				}
			});
		}
	}

	@Override
	public int testProfile(String profileUuid)
	{
		try
		{
			return runProfile(profileUuid, true);
		}
		catch( Exception ex )
		{
			throw new RuntimeApplicationException(ex.getMessage());
		}
	}
}
