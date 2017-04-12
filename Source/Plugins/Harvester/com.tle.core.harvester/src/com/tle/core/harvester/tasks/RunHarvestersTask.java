package com.tle.core.harvester.tasks;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.harvester.HarvesterProfileService;
import com.tle.core.scheduler.ScheduledTask;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RunHarvestersTask implements ScheduledTask
{
	private static final Logger LOGGER = Logger.getLogger(RunHarvestersTask.class);

	@Inject
	private HarvesterProfileService harvesterProfileService;
	@Inject
	private HarvesterProfileService harvesterService;

	@Override
	public void execute()
	{
		LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.runtask"));
		LOGGER.info("------------------");
		for( HarvesterProfile profile : harvesterProfileService.enumerateEnabledProfiles() )
		{
			harvesterService.startHarvesterTask(profile.getUuid(), false);
		}
	}
}
