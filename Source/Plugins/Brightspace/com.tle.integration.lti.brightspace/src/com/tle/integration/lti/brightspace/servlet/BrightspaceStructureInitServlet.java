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

package com.tle.integration.lti.brightspace.servlet;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.connectors.ConnectorCourse;
import com.tle.common.connectors.ConnectorFolder;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.connectors.brightspace.service.BrightspaceConnectorService;
import com.tle.core.connectors.exception.LmsUserNotFoundException;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.core.replicatedcache.ReplicatedCacheService;
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.integration.extension.StructuredIntegrationSessionExtension;
import com.tle.web.selection.SelectionSession;

/**
 * Served at /brightspacestructureinit
 * 
 * @author Aaron
 *
 */
@NonNullByDefault
@Bind
@Singleton
public class BrightspaceStructureInitServlet extends HttpServlet
{
	@Inject
	private ReplicatedCacheService cacheService;
	@Inject
	private StructuredIntegrationSessionExtension structuredInteg;
	@Inject
	private ConnectorService connectorService;
	@Inject
	private BrightspaceConnectorService brightspaceService;
	@Inject
	private UserSessionService userSessionService;

	private ReplicatedCache<String> courseStructureCache;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@PostConstruct
	public void setupCache()
	{
		courseStructureCache = cacheService.getCache("BrightspaceSignonCourseStructure", 100, 2, TimeUnit.MINUTES);
	}

	@Override
	protected void doGet(@SuppressWarnings("null") HttpServletRequest req,
		@SuppressWarnings("null") HttpServletResponse resp) throws ServletException, IOException
	{
		final String courseId = getMandatoryParameter(req, "courseId");
		final String connectorUuid = getMandatoryParameter(req, "connectorUuid");
		final String sessionId = getMandatoryParameter(req, "sessionId");
		final String fwd = getMandatoryParameter(req, "fwd");
		final String selected = req.getParameter("selected");

		final SelectionSession session = userSessionService.getAttribute(sessionId);
		if( session == null )
		{
			throw new RuntimeException("No session with session ID = " + sessionId);
		}
		final String structure = getStructure(courseId, connectorUuid, selected);
		structuredInteg.initStructure(session, structure);

		resp.sendRedirect(fwd);
	}

	private String getMandatoryParameter(HttpServletRequest req, String key)
	{
		final String value = req.getParameter(key);
		if( value == null )
		{
			throw new RuntimeException("Required parameter " + key + " not present");
		}
		return value;
	}

	private String getStructure(String courseId, String connectorUuid, String selected)
	{
		try
		{
			final boolean isDefaultApplicable = !Strings.isNullOrEmpty(selected);
			final String cacheKey = connectorUuid + ":" + courseId + (!isDefaultApplicable ? "" : ":" + selected);
			String structure = courseStructureCache.get(cacheKey).orNull();

			// if no structure, get from Brightspace
			if( structure == null )
			{
				final Connector connector = connectorService.getByUuid(connectorUuid);
				if( connector == null )
				{
					throw new RuntimeException("No connector with UUID = " + connectorUuid);
				}

				final ConnectorCourse course = brightspaceService.getCourse(connector, courseId);

				final ObjectNode root = objectMapper.createObjectNode();
				root.put("id", courseId);
				root.put("name", course.getName());
				root.put("targetable", false);
				final ArrayNode foldersNode = objectMapper.createArrayNode();
				root.put("folders", foldersNode);

				final List<ConnectorFolder> folders = brightspaceService.getFoldersForCourse(connector, null, courseId,
					false);
				boolean first = true;
				for( ConnectorFolder folder : folders )
				{
					final ObjectNode folderNode = objectMapper.createObjectNode();
					folderNode.put("id", folder.getId());
					folderNode.put("name", folder.getName());
					folderNode.put("targetable", true);
					folderNode.put("selected",
						(!isDefaultApplicable && first) || (isDefaultApplicable && selected.equals(folder.getId())));
					foldersNode.add(folderNode);
					first = false;

					recurseFolder(connector, courseId, folder, folderNode, selected);
				}

				final PrettyPrinter pp = new MinimalPrettyPrinter();
				try
				{
					structure = objectMapper.writer().with(pp).writeValueAsString(root);
				}
				catch( JsonProcessingException e )
				{
					throw Throwables.propagate(e);
				}

				if( structure == null )
				{
					// Can't happen
					throw new RuntimeException("Could not create structure");
				}
				courseStructureCache.put(cacheKey, structure);
			}

			return structure;
		}
		catch( LmsUserNotFoundException lms )
		{
			throw Throwables.propagate(lms);
		}
	}

	private void recurseFolder(Connector connector, String courseId, ConnectorFolder folder, ObjectNode folderNode,
		String selected)
	{
		try
		{
			final boolean isDefaultApplicable = !Strings.isNullOrEmpty(selected);
			final List<ConnectorFolder> subFolders = brightspaceService.getFoldersForFolder(connector, null, courseId,
				folder.getId(), false);
			if( subFolders.size() > 0 )
			{
				final ArrayNode subFoldersArrayNode = objectMapper.createArrayNode();
				folderNode.put("folders", subFoldersArrayNode);

				for( ConnectorFolder subFolder : subFolders )
				{
					final ObjectNode subFolderNode = objectMapper.createObjectNode();
					subFolderNode.put("id", subFolder.getId());
					subFolderNode.put("name", subFolder.getName());
					subFolderNode.put("targetable", true);
					subFolderNode.put("selected", isDefaultApplicable && selected.equals(subFolder.getId()));
					subFoldersArrayNode.add(subFolderNode);

					recurseFolder(connector, courseId, subFolder, subFolderNode, selected);
				}
			}
		}
		catch( LmsUserNotFoundException lms )
		{
			throw Throwables.propagate(lms);
		}
	}
}
