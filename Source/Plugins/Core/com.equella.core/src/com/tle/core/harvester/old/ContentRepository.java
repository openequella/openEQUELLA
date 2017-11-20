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

package com.tle.core.harvester.old;

import java.util.Collection;
import java.util.Date;

import com.dytech.devlib.PropBagEx;
import com.tle.common.harvester.HarvesterProfile;
import com.tle.common.searching.Search;

/**
 * @author Nicholas Read
 */
public interface ContentRepository
{

	/**
	 * Processes a learning object to see if it is currently in the repository,
	 * needs updating or is current. Then uploads the item and its attachments
	 * 
	 * @param lobject The object to process
	 */
	void processLearningObject(LearningObject lobject);

	/**
	 * Sets the profiles lastRun date to the current time
	 */
	void updateProfileRunDate(Date started);

	/**
	 * Used to set up a harvester task. Should implementations should override
	 * then call the parent method
	 * 
	 * @param profile The harvester profile
	 * @param testOnly Whether to run the harvester or just return the number of
	 *            results
	 * @return If testOnly is true, the number of results found is returned
	 */
	int setupAndRun(HarvesterProfile profile, boolean testOnly) throws Exception;

	/**
	 * Gets a list of Learning Objects from the content repository that have
	 * been added/changed since the given date.
	 * 
	 * @param since the date for getting new/modified objects since.
	 * @return a collection of LearningObjects
	 * @throws Exception if anything goes wrong.
	 */
	Collection<LearningObject> getUpdatedLearningObjects(Date since) throws Exception;

	/**
	 * Returns the SearchRequest for finding old versions of the given
	 * LearningObject in TLE.
	 */
	Search getTLESearchRequest(LearningObject lobject);

	/**
	 * Create a name for the attachment
	 * 
	 * @param lobject The item
	 * @return
	 */
	String createAttachmentName(LearningObject lobject);

	/**
	 * Called just before the item is uploaded to the repository.
	 * 
	 * @param xml The item xml
	 * @param lobject The learning object
	 */
	void postProcessing(PropBagEx xml, LearningObject lobject) throws Exception;

	/**
	 * Called to download the object from the remote repository. StagingID can
	 * be used to save any attachments.
	 * 
	 * @param lobject The object
	 * @param stagingID Staging id for the new item
	 */
	void downloadLO(LearningObject lobject, String stagingID) throws Exception;

}
