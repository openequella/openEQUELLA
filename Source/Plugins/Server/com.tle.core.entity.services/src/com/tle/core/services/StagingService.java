package com.tle.core.services;

import com.tle.core.filesystem.StagingFile;

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
