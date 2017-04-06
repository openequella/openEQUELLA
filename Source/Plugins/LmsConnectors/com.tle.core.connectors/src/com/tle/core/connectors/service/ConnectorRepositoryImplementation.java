package com.tle.core.connectors.service;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.searching.SearchResults;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorRepositoryService.ExternalContentSortType;
import com.tle.web.selection.SelectedResource;

@NonNullByDefault
public interface ConnectorRepositoryImplementation
{
	/**
	 * @param connector
	 * @param username
	 * @param courseId
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	@Nullable
	String getCourseCode(Connector connector, String username, String courseId) throws LmsUserNotFoundException;

	/**
	 * @param connector
	 * @param username
	 * @param editableOnly If true as list of courses that the user can add
	 *            content to should be returned. If false then ALL courses will
	 *            be returned.
	 * @param archived
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	List<ConnectorCourse> getCourses(Connector connector, String username, boolean editableOnly, boolean archived)
		throws LmsUserNotFoundException;

	List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId)
		throws LmsUserNotFoundException;

	List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId, String folderId)
		throws LmsUserNotFoundException;

	ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String folderId,
		IItem<?> item, SelectedResource selectedResource) throws LmsUserNotFoundException;

	List<ConnectorContent> findUsages(Connector connector, String username, String uuid, int version,
		boolean versionIsLatest, boolean archived, boolean allVersion) throws LmsUserNotFoundException;

	/**
	 * Note: count < 0 means ALL!
	 * 
	 * @param connector
	 * @param username
	 * @param query
	 * @param courseId
	 * @param folderId
	 * @param archived
	 * @param offset
	 * @param count
	 * @param sortType
	 * @param reverseSort
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, @Nullable String query,
		@Nullable String courseId, @Nullable String folderId, boolean archived, int offset, int count,
		ExternalContentSortType sortType, boolean reverseSort) throws LmsUserNotFoundException;

	int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived)
		throws LmsUserNotFoundException;

	boolean deleteContent(Connector connector, String username, String contentId) throws LmsUserNotFoundException;

	boolean editContent(Connector connector, String username, String contentId, String title, String description)
		throws LmsUserNotFoundException;

	/**
	 * @param connector
	 * @param username
	 * @param contentId
	 * @param courseId The new course ID
	 * @param folderId The new folder ID
	 * @return
	 * @throws LmsUserNotFoundException
	 */
	boolean moveContent(Connector connector, String username, String contentId, String courseId, String folderId)
		throws LmsUserNotFoundException;

	ConnectorTerminology getConnectorTerminology();

	boolean supportsExport();

	boolean supportsEdit();

	boolean supportsView();

	boolean supportsDelete();

	boolean supportsCourses();

	boolean supportsFindUses();
}
