package com.tle.common.harvester;

import java.util.List;

import com.tle.core.remoting.RemoteAbstractEntityService;

public interface RemoteHarvesterProfileService extends RemoteAbstractEntityService<HarvesterProfile>
{
	String ENTITY_TYPE = "HARVESTER_PROFILE"; //$NON-NLS-1$

	/**
	 * List the harvester profiles
	 * 
	 * @return
	 */
	List<HarvesterProfile> enumerateEnabledProfiles();

	/**
	 * Runs a harvester profile
	 * 
	 * @param profile The profile
	 * @throws Exception
	 */
	int testProfile(String profileUuid);

	/**
	 * Runs a harvester profile as a SpringClusteredTask
	 * 
	 * @param harvesterProfile The profile
	 */
	void startHarvesterTask(String profileUuid, boolean manualKickoff);

}
