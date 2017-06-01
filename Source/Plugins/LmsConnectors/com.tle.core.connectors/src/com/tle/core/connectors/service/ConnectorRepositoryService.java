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

package com.tle.core.connectors.service;

import java.util.List;

import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SearchResults;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;

public interface ConnectorRepositoryService
{
	public enum ExternalContentSortType
	{
		NAME, COURSE, DATE_ADDED;

		@Override
		public String toString()
		{
			return super.toString().toLowerCase();
		}
	}

	/**
	 * Does this connector require an OAuth style dialog to be shown before it can continue?
	 * @param connector
	 * @return
	 */
	boolean isRequiresAuthentication(Connector connector);

	String getAuthorisationUrl(Connector connector, @Nullable String forwardUrl, @Nullable String authData);

	String mungeUsername(String username, Connector connector);

	/**
	 * Assumed management=true
	 * 
	 * @param connector
	 * @param username
	 * @param archived
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	List<ConnectorCourse> getAllCourses(Connector connector, String username, boolean archived)
		throws LmsUserNotFoundException;

	List<ConnectorCourse> getModifiableCourses(Connector connector, String username, boolean archived,
		boolean management) throws LmsUserNotFoundException;

	ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String sectionId,
		IItem<?> item, IAttachment attachment, SectionInfo info) throws LmsUserNotFoundException;

	ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String sectionId,
		IItem<?> item, SelectedResource resource) throws LmsUserNotFoundException;

	List<String> getImplementationTypes();

	List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId,
		boolean management);

	List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId, String folderId,
		boolean management);

	List<ConnectorContent> findUsages(Connector connector, String username, String itemUuid, int itemVersion,
		boolean archived, boolean allVersions) throws LmsUserNotFoundException;

	List<ConnectorContent> findUsages(Connector connector, String username, IItem<?> item, boolean archived,
		boolean allVersions) throws LmsUserNotFoundException;

	/**
	 * Assumed management=true
	 * 
	 * @param connector
	 * @param username
	 * @param query May be blank
	 * @param courseId May be blank
	 * @param folderId May be blank
	 * @param archived
	 * @param offset
	 * @param count if one of the connectors isn't working.
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, String query, String courseId,
		String folderId, boolean archived, int offset, int count, ExternalContentSortType sortType,
		boolean sortAscending) throws LmsUserNotFoundException;

	/**
	 * Purely for Birt
	 * 
	 * @param username
	 * @param query
	 * @param courseId
	 * @param folderId
	 * @param archived
	 * @param offset
	 * @param count
	 * @param sortType
	 * @param sortAscending
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	SearchResults<ConnectorContent> findAllUsagesAllConnectors(String username, String query, String courseId,
		String folderId, boolean archived, int offset, int count, ExternalContentSortType sortType,
		boolean sortAscending) throws LmsUserNotFoundException;

	/**
	 * @param connector
	 * @param username
	 * @param archived
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived)
		throws LmsUserNotFoundException;

	ConnectorTerminology getConnectorTerminology(String lmsType);

	/**
	 * Controls move and export access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsExport(String lmsType);

	boolean moveContent(Connector connector, String username, String contentId, String courseId, String locationId)
		throws LmsUserNotFoundException;

	/**
	 * Controls edit access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsEdit(String lmsType);

	boolean editContent(Connector connector, String username, String contentId, String title, String description)
		throws LmsUserNotFoundException;

	/**
	 * Controls ?? access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsView(String lmsType);

	/**
	 * Controls delete access
	 * 
	 * @param connector
	 * @return
	 */
	boolean supportsDelete(String lmsType);

	boolean deleteContent(Connector connector, String username, String id) throws LmsUserNotFoundException;

	boolean supportsCourses(String lmsType);

	boolean supportsFindUses(String lmsType);

	boolean supportsReverseSort(String lmsType);

	boolean supportsEditDescription(String lmsType);

	//TODO: change all the supportsX calls to getCapabilities which returns an object with boolen fields on it.
}
