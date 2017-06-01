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

package com.tle.core.connectors.brightspace.service;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.brightspace.beans.BrightspaceQuicklink;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryImplementation;
import com.tle.web.integration.Integration.LmsLink;

@NonNullByDefault
public interface BrightspaceConnectorService extends ConnectorRepositoryImplementation
{
	public enum TopicCreationOption
	{
		NONE, CREATE
	}

	/**
	 * Admin setup function
	 * 
	 * @param appId
	 * @param appKey
	 * @param brightspaceServerUrl
	 * @param userId
	 * @param userKey
	 * @return
	 */
	String whoAmI(String appId, String appKey, String brightspaceServerUrl, String userId, String userKey);

	/**
	 * Admin setup function
	 * 
	 * @param serverUrl
	 * @param appId
	 * @param appKey
	 * @return
	 */
	String testApplication(String appId, String appKey, String brightspaceServerUrl);

	/**
	 * Admin setup function
	 * 
	 * @param appId
	 * @param appKey
	 * @param brightspaceServerUrl
	 * @param forwardUrl
	 * @param postfixKey
	 * @return
	 */
	String getAuthorisationUrl(String appId, String appKey, String brightspaceServerUrl, String forwardUrl,
		@Nullable String postfixKey);

	/**
	 * 
	 * @return A sparsely populated course
	 */
	ConnectorCourse getCourse(Connector connector, String courseId);

	/**
	 * 
	 * @param connector
	 * @param courseId
	 * @param moduleId ONLY nullable if topicOption == TopicCreationOption.NONE
	 * @param link
	 * @param topicOption
	 * @return
	 */
	BrightspaceQuicklink addQuicklink(Connector connector, String courseId, @Nullable String moduleId, LmsLink link,
		TopicCreationOption topicOption);

	// Override these since username is nullable (and useless)

	@Override
	List<ConnectorFolder> getFoldersForCourse(Connector connector, @Nullable String username, String courseId,
		boolean management) throws LmsUserNotFoundException;

	@Override
	List<ConnectorFolder> getFoldersForFolder(Connector connector, @Nullable String username, String courseId,
		String folderId, boolean management) throws LmsUserNotFoundException;

	/**
	 * The connector object will need to store an encrypted admin token in the DB.  Use this method to encrypt the one returned from Brightspace.
	 * @param token
	 * @return
	 */
	String encrypt(String data);

	String decrypt(String encryptedData);
}
