package com.tle.core.filesystem.staging.service;

import com.tle.common.filesystem.handle.StagingFile;

/**
 * @author Nicholas Read
 */
public interface StagingService
{
	StagingFile createStagingArea();

	void removeStagingArea(StagingFile staging, boolean deleteFiles);

	void removeAllStagingAreas(String sessionId);

	boolean stagingExists(String stagingId);

	void removeUnusedStagingAreas();
}
