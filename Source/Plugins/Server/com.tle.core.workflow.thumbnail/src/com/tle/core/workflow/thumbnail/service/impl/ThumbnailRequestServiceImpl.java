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

package com.tle.core.workflow.thumbnail.service.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.common.io.FileUtils;
import com.dytech.common.io.FileUtils.GrepFunctor;
import com.dytech.devlib.Md5;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemKey;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.filesystem.InstitutionFile;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.institution.InstitutionStatus;
import com.tle.core.services.FileSystemService;
import com.tle.core.workflow.thumbnail.ThumbnailQueueFile;
import com.tle.core.workflow.thumbnail.dao.ThumbnailRequestDao;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;
import com.tle.core.workflow.thumbnail.service.ThumbnailRequestService;

/**
 * Do not use directly. You should invoke methods on the ThumbnailService.
 *
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind(ThumbnailRequestService.class)
@Singleton
/* package protected */class ThumbnailRequestServiceImpl implements ThumbnailRequestService
{
	private static final Logger LOGGER = Logger.getLogger(ThumbnailRequestServiceImpl.class);

	@Inject
	private ThumbnailRequestDao thumbRequestDao;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService instService;

	@Transactional
	@Override
	public void newRequest(String filename, ItemKey itemId, FileHandle handle, int thumbFlags, boolean forceIt,
		boolean clearPending)
	{
		LOGGER.info("Queueing thumbnail generation for: " + filename + " with flags " + thumbFlags);
		final String requestUuid = UUID.randomUUID().toString();
		final ThumbnailQueueFile thumbQueueFile = new ThumbnailQueueFile(requestUuid);

		//copy original file to ThumbQueue file
		//(TODO: this could be a large video... we'd want to avoid this copy if it were possible.  COWFS anyone?)
		fileSystemService.copy(handle, filename, thumbQueueFile, filename);

		if( clearPending )
		{
			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Clearing redundant requests for " + filename);
			}
			cancelRedundantRequests(itemId, handle, filename);
		}

		final ThumbnailRequest tr = new ThumbnailRequest();
		tr.setDateRequested(new Date());
		tr.setInstitution(CurrentInstitution.get());
		tr.setUuid(requestUuid);
		tr.setFilename(filename);
		tr.setFilenameHash(new Md5(filename).getStringDigest());
		tr.setItemUuid(itemId.getUuid());
		tr.setItemVersion(itemId.getVersion());
		tr.setHandle(serialiseHandle(handle));
		tr.setRecreate(forceIt);
		tr.setThumbnailTypes(thumbFlags);
		thumbRequestDao.save(tr);
	}

	/**
	 * Cancels all requests for the item that don't belong to the supplied handle+filename
	 *
	 * @param itemId
	 * @param handle
	 * @param filename
	 */
	@Transactional
	protected void cancelRedundantRequests(ItemKey itemId, FileHandle handle, String filename)
	{
		LOGGER.trace("Cancelling redundant requests");
		for( ThumbnailRequest request : listForFile(itemId, handle, filename) )
		{
			delete(request, request.getUuid());
		}
	}

	private String serialiseHandle(FileHandle handle)
	{
		final StringBuilder superSerialHandle = new StringBuilder();
		if( handle instanceof StagingFile )
		{
			superSerialHandle.append("staging:");
			StagingFile staging = (StagingFile) handle;
			superSerialHandle.append(staging.getUuid());
		}
		else if( handle instanceof ItemFile )
		{
			superSerialHandle.append("item:");
			ItemFile item = (ItemFile) handle;
			superSerialHandle.append(item.getItemId().toString());
		}
		else
		{
			throw new RuntimeException("Not a staging or item file handle");
		}
		return superSerialHandle.toString();
	}

	@Transactional
	@Override
	public void update(ThumbnailRequest thumbnailRequest)
	{
		thumbRequestDao.update(thumbnailRequest);
	}

	@Transactional
	@Override
	public void delete(String requestUuid)
	{
		// At this point, the request could already be deleted.  Get it again so we avoid an error.
		// There is probably an inbuilt Hibernate thing to do this check...
		final ThumbnailRequest tr = getByUuid(requestUuid);
		delete(tr, requestUuid);
	}

	/**
	 * You can directly use this method if you know that it definitely still exists.
	 * The Nullable is due to the string parameter version using this method.
	 * @param tr
	 */
	@Transactional
	protected void delete(@Nullable ThumbnailRequest tr, String requestUuid)
	{
		try
		{
			// Remove the offending thumb queue files
			final ThumbnailQueueFile thumbQueueFile = new ThumbnailQueueFile(requestUuid);

			if( LOGGER.isDebugEnabled() )
			{
				LOGGER.debug("Cleaning up files for thumbnail request " + (tr == null ? requestUuid : tr.toString()));
			}
			fileSystemService.removeFile(thumbQueueFile);
		}
		catch( Exception e )
		{
			LOGGER.warn("Error cleaning up thumbnail request files " + (tr == null ? requestUuid : tr.toString()), e);
		}
		finally
		{
			//Do the needful with the DB
			if( tr != null )
			{
				try
				{
					if( LOGGER.isDebugEnabled() )
					{
						LOGGER.debug("Deleting thumbnail request from DB " + tr.toString());
					}
					thumbRequestDao.delete(tr);
				}
				catch( Exception e )
				{
					LOGGER.warn("Error deleting thumbnail request", e);
				}
			}
		}
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> listForHandle(ItemKey itemId, FileHandle handle)
	{
		return thumbRequestDao.listForHandle(CurrentInstitution.get(), itemId, serialiseHandle(handle));
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> listForFile(ItemKey itemId, FileHandle handle, String filename)
	{
		final List<ThumbnailRequest> forFile = new ArrayList<>(
			thumbRequestDao.listForFile(CurrentInstitution.get(), itemId, new Md5(filename).getStringDigest()));
		final String superSerialHandle = serialiseHandle(handle);
		//TODO: would be more efficient with a DB query
		final Iterator<ThumbnailRequest> iterator = forFile.iterator();
		while( iterator.hasNext() )
		{
			final ThumbnailRequest next = iterator.next();
			if( next.getHandle().equals(superSerialHandle) )
			{
				iterator.remove();
			}
		}
		return forFile;
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> list(Institution institution)
	{
		return thumbRequestDao.list(institution);
	}

	@Transactional
	@Override
	public List<ThumbnailRequest> list(Institution institution, ItemKey itemId)
	{
		return thumbRequestDao.list(institution, itemId);
	}

	@Transactional
	@Override
	public ThumbnailRequest getByUuid(String requestUuid)
	{
		return thumbRequestDao.getByUuid(requestUuid);
	}

	@Override
	public boolean exists(ItemKey itemId, FileHandle handle, String filename)
	{
		final String superSerial = serialiseHandle(handle);
		return thumbRequestDao.exists(itemId, superSerial, filename, new Md5(filename).getStringDigest());
	}

	@Transactional
	@Override
	public void cleanThumbQueue()
	{
		Collection<InstitutionStatus> allInstitutions = instService.getAllInstitutions();

		for( InstitutionStatus inst : allInstitutions )
		{
			fileSystemService.apply(new InstitutionFile(inst.getInstitution()), "ThumbQueue", "*/*", new GrepFunctor()
			{
				@Override
				public void matched(Path file, String relFilepath)
				{
					String uuid = file.getFileName().toString();
					if( thumbRequestDao.getByUuid(uuid) == null )
					{
						FileUtils.delete(file);
					}
				}
			});
		}
	}
}
