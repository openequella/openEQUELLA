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

package com.tle.core.harvester;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.inject.Inject;

import com.tle.core.plugins.AbstractPluginService;
import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.harvester.old.ContentRepository;
import com.tle.core.harvester.old.LearningObject;
import com.tle.core.harvester.old.TLEItem;
import com.tle.core.harvester.search.HarvesterSearch;
import com.tle.core.services.FileSystemService;

@SuppressWarnings("nls")
public abstract class AbstractHarvesterProtocol implements ContentRepository
{
	private static final Logger LOGGER = Logger.getLogger(AbstractHarvesterProtocol.class);
	protected static final String KEY_PFX = AbstractPluginService.getMyPluginId(AbstractHarvesterProtocol.class)+".";

	@Inject
	private LearningEdge learningEdge;
	@Inject
	private FileSystemService fileSystemService;

	private HarvesterProfile profile;

	private ItemDefinition itemDefByUuid;
	private long schema;

	@Override
	public int setupAndRun(HarvesterProfile profile, boolean testOnly) throws Exception
	{
		this.profile = profile;

		String itemdef = profile.getAttribute("itemDef");
		if( itemdef == null )
		{
			throw new RuntimeException(CurrentLocale.get(KEY_PFX + "error.collection"));
		}
		itemDefByUuid = learningEdge.getItemDefByUuid(itemdef);
		schema = learningEdge.getSchemaByUuid(itemdef);
		return runProfile(testOnly);
	}

	private int runProfile(boolean testOnly) throws Exception
	{
		Date lastRun = profile.getLastRun();
		Date started = new Date();

		Collection<LearningObject> results = getUpdatedLearningObjects(lastRun);

		int i = 0;
		int size = results.size();

		if( testOnly )
		{
			return size;
		}

		LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.run", profile.getAttribute("server")));
		LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.results", size, lastRun.toString()));

		for( LearningObject lobject : results )
		{
			boolean boo = false;
			processLearningObject(lobject);
			if( boo )
			{
				break;
			}

			LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.process", ++i, profile.getAttribute("server")));
		}

		LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.total", i, profile.getAttribute("server")));

		updateProfileRunDate(started);
		return size;
	}

	@Override
	public void updateProfileRunDate(Date started)
	{
		learningEdge.updateProfileRunDate(profile, started);
	}

	/**
	 * Takes a learning object, and uploads it to our repository if it TLE's
	 * version is out of date.
	 */

	@Override
	public void processLearningObject(LearningObject lobject)
	{
		String identifier = lobject.getIdentifier();
		try
		{
			// This will be our new item
			PropBagEx item;
			TLEItem oldItem = null;

			if( lobject.isEquellaItem() )
			{
				if( learningEdge.itemExists(identifier) )
				{
					oldItem = learningEdge.getItem(identifier, itemDefByUuid);
				}
			}
			else
			{
				// Search for any older versions of this item.
				HarvesterSearch request = (HarvesterSearch) getTLESearchRequest(lobject);
				request.setCollectionUuids(Collections.singleton(itemDefByUuid.getUuid()));
				oldItem = learningEdge.getLatestItem(request);
			}

			if( oldItem != null )
			{
				Date creationDate = lobject.getCreationDate();
				Date creationDate2 = oldItem.getCreationDate();

				Date modDate1 = lobject.getModifiedDate();
				Date modDate2 = oldItem.getModifiedDate();

				if( creationDate2.compareTo(creationDate) < 0
					|| ((modDate2 != null && modDate1 != null) && modDate2.compareTo(modDate1) < 0) )
				{
					LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.previous", identifier));
					// Does profile require a new version? Or update-in-place?
					// Should a null value exist (it shouldn't, but ...) assume
					// new Version logic
					boolean newVersionOnHarvest = (profile.getNewVersionOnHarvest() == null || profile
						.getNewVersionOnHarvest().booleanValue());
					if( newVersionOnHarvest )
					{
						// new incremented version of existing item
						item = learningEdge.newVersion(oldItem);
					}
					else
					{
						// overwrite existing item with new one (no versioning -
						// obliterates old item)
						item = learningEdge.modifyInPlace(oldItem);
					}
				}
				else
				{
					LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.exists", identifier));
					return;
				}
			}
			else
			{
				LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.noprevious", identifier));

				if( lobject.isEquellaItem() )
				{
					item = learningEdge.newItem(identifier, itemDefByUuid);
				}
				else
				{
					item = learningEdge.newItem(itemDefByUuid);
				}
			}

			// Download the item attachment (if applicable)
			if( lobject.hasAttachment() )
			{
				String staging = item.getNode("item/staging");
				downloadLO(lobject, staging);
			}

			// Do any post-processing to the XML
			postProcessing(item, lobject);

			// Upload the XML
			LOGGER.info(CurrentLocale.get("com.tle.core.harvester.log.uploading", lobject.getTitle()));
			learningEdge.uploadItem(item, itemDefByUuid);
			LOGGER.info(CurrentLocale.get(KEY_PFX + "log.complete"));

		}
		catch( Exception ex )
		{
			LOGGER.error(
				CurrentLocale.get("com.tle.core.harvester.harvester.error.upload", identifier, profile.getAttribute("server")),
				ex);
		}
	}

	protected OutputStream getOutputStream(StagingFile staging, String filename) throws IOException
	{
		return fileSystemService.getOutputStream(staging, filename, false);
	}

	protected String transformSchema(PropBagEx xml, String xsltName) throws Exception
	{
		return learningEdge.transformSchema(schema, xml, xsltName);
	}

	protected HarvesterProfile getProfile()
	{
		return profile;
	}
}
