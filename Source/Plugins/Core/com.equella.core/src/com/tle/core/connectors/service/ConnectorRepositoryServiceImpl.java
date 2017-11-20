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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.edge.common.ScriptContext;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.item.IItem;
import com.tle.beans.item.VersionSelection;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.scripting.service.ScriptingService;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.common.settings.standard.QuickContributeAndVersionSettings;
import com.tle.core.activation.service.CourseInfoService;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.scripting.service.StandardScriptContextParams;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.security.impl.SecureOnReturn;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;

@SuppressWarnings("nls")
@Bind(ConnectorRepositoryService.class)
public class ConnectorRepositoryServiceImpl implements ConnectorRepositoryService
{
	private static final String USERNAME_SCRIPT_VARIABLE = "username";
	private static final Logger LOGGER = Logger.getLogger(ConnectorRepositoryService.class);

	private PluginTracker<ConnectorRepositoryImplementation> implTracker;
	@Inject
	private ScriptingService scriptingService;
	@Inject
	private CourseInfoService courseInfoService;
	@Inject
	private ConfigurationService configurationService;
	@Inject
	private ItemService itemService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private ConnectorService connectorService;

	@Override
	public boolean isRequiresAuthentication(Connector connector)
	{
		return getImplementation(connector.getLmsType()).isRequiresAuthentication(connector);
	}

	@Override
	public String getAuthorisationUrl(Connector connector, String forwardUrl, String authData)
	{
		return getImplementation(connector.getLmsType()).getAuthorisationUrl(connector, forwardUrl, authData);
	}

	@Override
	public String mungeUsername(String username, Connector connector)
	{
		if( !connector.isUseLoggedInUsername() )
		{
			final String usernameScript = connector.getUsernameScript();
			if( !Check.isEmpty(usernameScript) )
			{
				final ScriptContext scriptContext = scriptingService
					.createScriptContext(new StandardScriptContextParams(null, null, false, null));
				scriptContext.addScriptObject(USERNAME_SCRIPT_VARIABLE, username);
				final Object munged = scriptingService.executeScript(usernameScript, "connector username script",
					scriptContext, true, String.class);
				if( munged instanceof String )
				{
					return (String) munged;
				}
			}
		}
		return username;
	}

	private boolean isAlwaysLatest()
	{
		final VersionSelection versionSelection = configurationService
			.getProperties(new QuickContributeAndVersionSettings()).getVersionSelection();
		if( versionSelection != null )
		{
			return versionSelection == VersionSelection.FORCE_LATEST
				|| versionSelection == VersionSelection.DEFAULT_TO_LATEST;
		}
		return false;
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public List<ConnectorCourse> getAllCourses(Connector connector, String username, boolean archived)
		throws LmsUserNotFoundException
	{
		List<ConnectorCourse> courses = getImplementation(connector.getLmsType()).getCourses(connector,
			mungeUsername(username, connector), false, archived, true);

		Collections.sort(courses, new Comparator<ConnectorCourse>()
		{
			@Override
			public int compare(ConnectorCourse o1, ConnectorCourse o2)
			{
				return o1.getName().compareToIgnoreCase(o2.getName());
			}

		});

		return courses;
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public List<ConnectorCourse> getModifiableCourses(Connector connector, String username, boolean archived,
		boolean management) throws LmsUserNotFoundException
	{
		List<ConnectorCourse> courses = getImplementation(connector.getLmsType()).getCourses(connector,
			mungeUsername(username, connector), true, archived, management);

		Collections.sort(courses, new Comparator<ConnectorCourse>()
		{
			@Override
			public int compare(ConnectorCourse o1, ConnectorCourse o2)
			{
				return o1.getName().compareToIgnoreCase(o2.getName());
			}

		});

		return courses;
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR)
	@Override
	public ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String sectionId,
		IItem<?> item, IAttachment attachment, SectionInfo info) throws LmsUserNotFoundException
	{
		final SelectedResource selectedResource = (attachment == null
			? selectionService.createItemSelection(info, item, null)
			: new SelectedResource(item.getItemId(), attachment, null, null));

		return addItemToCourse(connector, username, courseId, sectionId, item, selectedResource);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR)
	@Override
	public ConnectorFolder addItemToCourse(Connector connector, String username, String courseId, String sectionId,
		IItem<?> item, SelectedResource selectedResource) throws LmsUserNotFoundException
	{
		final String mungedUsername = mungeUsername(username, connector);
		final ConnectorRepositoryImplementation implementation = getImplementation(connector.getLmsType());
		final String courseCode = implementation.getCourseCode(connector, mungedUsername, courseId);

		boolean alwaysLatest = isAlwaysLatest();
		if( !Check.isEmpty(courseCode) )
		{
			final CourseInfo course = courseInfoService.getByCode(courseCode);
			if( course != null )
			{
				final VersionSelection versionSelection = course.getVersionSelection();
				if( versionSelection != null )
				{
					alwaysLatest = (versionSelection == VersionSelection.FORCE_LATEST
						|| versionSelection == VersionSelection.DEFAULT_TO_LATEST);
				}
			}
		}

		selectedResource.setLatest(alwaysLatest);
		return implementation.addItemToCourse(connector, mungedUsername, courseId, sectionId, item, selectedResource);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public List<ConnectorContent> findUsages(Connector connector, String username, IItem<?> item, boolean archived,
		boolean allVersions) throws LmsUserNotFoundException
	{
		return findUsages(connector, username, item.getUuid(), item.getVersion(), archived, allVersions);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public List<ConnectorContent> findUsages(Connector connector, String username, String itemUuid, int itemVersion,
		boolean archived, boolean allVersions) throws LmsUserNotFoundException
	{
		final IItem<?> latestVersion = findItemForUsage(itemUuid);
		return getImplementation(connector.getLmsType()).findUsages(connector, mungeUsername(username, connector),
			itemUuid, itemVersion, itemVersion == latestVersion.getVersion(), archived, allVersions);
	}

	@SecureOnReturn(priv = ConnectorConstants.PRIV_FIND_USES_ITEM)
	protected IItem<?> findItemForUsage(String itemUuid)
	{
		return itemService.getLatestVersionOfItem(itemUuid);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public SearchResults<ConnectorContent> findAllUsages(Connector connector, String username, String query,
		String courseId, String folderId, boolean archived, int offset, int count, ExternalContentSortType sortType,
		boolean sortAscending) throws LmsUserNotFoundException
	{
		return getImplementation(connector.getLmsType()).findAllUsages(connector, mungeUsername(username, connector),
			query, courseId, folderId, archived, offset, count, sortType, sortAscending);
	}

	@Override
	public SearchResults<ConnectorContent> findAllUsagesAllConnectors(String username, String query, String courseId,
		String folderId, boolean archived, int offset, int count, ExternalContentSortType sortType,
		boolean sortAscending) throws LmsUserNotFoundException
	{
		// available count is zero for an 'all connectors' search, this is
		// used for reporting only

		final List<ConnectorContent> content = Lists.newArrayList();

		// Ugh. Merge *all* usages...
		final List<BaseEntityLabel> connectors = connectorService.listForViewing();
		for( BaseEntityLabel conn : connectors )
		{
			final Connector c = connectorService.get(conn.getId());
			try
			{
				final String mungedUsername = mungeUsername(username, c);
				final SearchResults<ConnectorContent> usages = getImplementation(c.getLmsType()).findAllUsages(c,
					mungedUsername, query, courseId, folderId, true, 0, -1, sortType, sortAscending);

				content.addAll(usages.getResults());

				if( content.size() > offset + count )
				{
					// You need to copy it. .subList() method returns a list
					// *backed* by the original list
					return new SimpleSearchResults<ConnectorContent>(
						Lists.newArrayList(content.subList(offset, offset + count)), count, offset, 0);
				}
			}
			catch( Exception t )
			{
				// Note: cannot output connector name because session is rolled
				// back on exception
				LOGGER.error("Error getting results from connector " + c.getId(), t);
			}
		}

		final int size = content.size();

		// do our best to honour the offset + count
		if( size > offset && size <= offset + count )
		{
			// You need to copy it. .subList() method returns a list
			// *backed* by the original list
			return new SimpleSearchResults<ConnectorContent>(Lists.newArrayList(content.subList(offset, size)),
				size - offset, offset, 0);
		}
		// if (size < offset)
		return new SimpleSearchResults<ConnectorContent>(new ArrayList<ConnectorContent>(), 0, offset, 0);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public int getUnfilteredAllUsagesCount(Connector connector, String username, String query, boolean archived)
		throws LmsUserNotFoundException
	{
		return getImplementation(connector.getLmsType()).getUnfilteredAllUsagesCount(connector,
			mungeUsername(username, connector), query, archived);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR)
	@Override
	public boolean deleteContent(Connector connector, String username, String id) throws LmsUserNotFoundException
	{
		return getImplementation(connector.getLmsType()).deleteContent(connector, mungeUsername(username, connector),
			id);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR)
	@Override
	public boolean editContent(Connector connector, String username, String contentId, String title, String description)
		throws LmsUserNotFoundException
	{
		return getImplementation(connector.getLmsType()).editContent(connector, mungeUsername(username, connector),
			contentId, title, description);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_EXPORT_VIA_CONNECTOR)
	@Override
	public boolean moveContent(Connector connector, String username, String contentId, String courseId,
		String locationId) throws LmsUserNotFoundException
	{
		return getImplementation(connector.getLmsType()).moveContent(connector, mungeUsername(username, connector),
			contentId, courseId, locationId);
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public List<ConnectorFolder> getFoldersForCourse(Connector connector, String username, String courseId,
		boolean management)
	{
		try
		{
			return getImplementation(connector.getLmsType()).getFoldersForCourse(connector,
				mungeUsername(username, connector), courseId, management);
		}
		catch( LmsUserNotFoundException e )
		{
			// the reality is, this will never happen
			throw Throwables.propagate(e);
		}
	}

	@SecureOnCall(priv = ConnectorConstants.PRIV_VIEWCONTENT_VIA_CONNECTOR)
	@Override
	public List<ConnectorFolder> getFoldersForFolder(Connector connector, String username, String courseId,
		String folderId, boolean management)
	{
		try
		{
			return getImplementation(connector.getLmsType()).getFoldersForFolder(connector,
				mungeUsername(username, connector), courseId, folderId, management);
		}
		catch( LmsUserNotFoundException e )
		{
			// the reality is, this will never happen
			throw Throwables.propagate(e);
		}
	}

	@Override
	public boolean supportsExport(String lmsType)
	{
		return getImplementation(lmsType).supportsExport();
	}

	@Override
	public boolean supportsEdit(String lmsType)
	{
		return getImplementation(lmsType).supportsEdit();
	}

	@Override
	public boolean supportsView(String lmsType)
	{
		return getImplementation(lmsType).supportsView();
	}

	@Override
	public boolean supportsDelete(String lmsType)
	{
		return getImplementation(lmsType).supportsDelete();
	}

	@Override
	public boolean supportsCourses(String lmsType)
	{
		return getImplementation(lmsType).supportsCourses();
	}

	@Override
	public ConnectorTerminology getConnectorTerminology(String lmsType)
	{
		return getImplementation(lmsType).getConnectorTerminology();
	}

	@Override
	public boolean supportsFindUses(String lmsType)
	{
		return true;
		//return getImplementation(lmsType).supportsFindUses();
	}

	@Override
	public boolean supportsReverseSort(String lmsType)
	{
		return getImplementation(lmsType).supportsReverseSort();
	}

	@Override
	public boolean supportsEditDescription(String lmsType)
	{
		return getImplementation(lmsType).supportsEditDescription();
	}

	private ConnectorRepositoryImplementation getImplementation(String type)
	{
		final ConnectorRepositoryImplementation impl = implTracker.getBeanMap().get(type);
		if( impl == null )
		{
			throw new RuntimeException("No connector repository implementation for type " + type);
		}
		return impl;
	}

	@Override
	public List<String> getImplementationTypes()
	{
		final List<String> implTypes = new ArrayList<String>();
		for( Map.Entry<String, ?> impl : implTracker.getBeanMap().entrySet() )
		{
			implTypes.add(impl.getKey());
		}
		return implTypes;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		implTracker = new PluginTracker<ConnectorRepositoryImplementation>(pluginService,
			"com.tle.core.connectors", "connectorImplementation", "type");
		implTracker.setBeanKey("class");
	}

}
