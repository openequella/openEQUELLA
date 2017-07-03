package com.tle.core.filesystem.staging.service.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.common.io.FileUtils;
import com.dytech.common.io.FileUtils.GrepFunctor;
import com.tle.beans.Staging;
import com.tle.common.filesystem.handle.AllStagingFile;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.filesystem.staging.dao.StagingDao;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;

/**
 * @author Nicholas Read
 */
@Bind(StagingService.class)
@Singleton
@SuppressWarnings("nls")
public class StagingServiceImpl implements StagingService
{
	private static final Logger LOGGER = Logger.getLogger(StagingServiceImpl.class);

	@Inject
	private StagingDao stagingDao;
	@Inject
	private FileSystemService fileSystemService;

	@Override
	@Transactional
	public StagingFile createStagingArea()
	{
		Staging s = new Staging();
		s.setStagingID(UUID.randomUUID().toString());
		s.setUserSession(CurrentUser.getSessionID());

		stagingDao.save(s);

		StagingFile file = new StagingFile(s.getStagingID());
		fileSystemService.mkdir(file, "");
		return file;
	}

	@Override
	@Transactional
	public void removeStagingArea(StagingFile staging, boolean removeFiles)
	{
		Staging s = stagingDao.findById(staging.getUuid());
		if( s == null )
		{
			LOGGER.error("Staging area does not exist");
		}
		else
		{
			stagingDao.delete(s);
		}

		if( removeFiles )
		{
			fileSystemService.removeFile(staging);
		}
	}

	@Override
	@Transactional
	public boolean stagingExists(String stagingId)
	{
		return stagingDao.findById(stagingId) != null;
	}

	@Transactional
	@Override
	public void removeAllStagingAreas(String sessionId)
	{
		stagingDao.deleteAllForUserSession(sessionId);
	}

	@Override
	@Transactional
	public void removeUnusedStagingAreas()
	{
		fileSystemService.apply(new AllStagingFile(), "", "*/*", new GrepFunctor()
		{
			@Override
			public void matched(Path file, String relFilepath)
			{
				String uuid = file.getFileName().toString();
				if( !stagingExists(uuid) )
				{
					try
					{
						FileUtils.delete(file, null, true);
					}
					catch( IOException ex )
					{
						LOGGER.warn("Could not delete staging area: " + uuid, ex);
					}
				}
			}
		});
	}
}
