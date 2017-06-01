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

package com.tle.web.integration.extension;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.web.integration.IntegrationSessionExtension;
import com.tle.web.integration.SingleSignonForm;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.TargetFolder;
import com.tle.web.selection.TargetStructure;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class StructuredIntegrationSessionExtension implements IntegrationSessionExtension
{
	public static final String KEY_COURSE_CODE = "courseCode";

	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(StructuredIntegrationSessionExtension.class);

	@Inject
	private ObjectMapperService objectMapperService;

	@Override
	public void setupSession(SectionInfo info, SelectionSession session, SingleSignonForm form)
	{
		final String structure = form.getStructure();
		initStructure(session, structure);
	}

	public void initStructure(SelectionSession session, String structureJson)
	{
		if( structureJson != null && structureJson.length() > 0 )
		{
			final ObjectMapper mapper = objectMapperService.createObjectMapper();
			final ObjectNode structureNode;
			try
			{
				structureNode = (ObjectNode) mapper.readTree(structureJson);
			}
			catch( IOException e )
			{
				throw Throwables.propagate(e);
			}
			final TargetStructure struct = session.getStructure();
			struct.putAttribute(KEY_COURSE_CODE, nodeValue(structureNode, "code", null));
			recurseFolders(structureNode, struct, session);

			final TargetFolder firstTargetable = findFirstTargetable(struct);
			if( firstTargetable == null )
			{
				struct.setNoTargets(true);
			}
			// recurseFolders should set the target folder if a default folder
			// is specified, otherwise we just need to pick the first targetable
			// folder.
			else if( session.getTargetFolder() == null )
			{
				session.setTargetFolder(firstTargetable.getId());
			}
		}
	}

	@Nullable
	private String nodeValue(ObjectNode obj, String fieldName, @Nullable String defaultValue)
	{
		final JsonNode node = obj.get(fieldName);
		if( node == null )
		{
			return defaultValue;
		}
		final String val = node.asText();
		if( val == null )
		{
			return defaultValue;
		}
		return val;
	}

	private boolean nodeValue(ObjectNode obj, String fieldName, boolean defaultValue)
	{
		final JsonNode node = obj.get(fieldName);
		if( node == null )
		{
			return defaultValue;
		}
		return node.asBoolean(defaultValue);
	}

	private void recurseFolders(ObjectNode structureJson, TargetFolder folder, SelectionSession session)
	{
		folder.setName(nodeValue(structureJson, "name", resources.getString("selection.folder.untitled")));
		final boolean targetable = nodeValue(structureJson, "targetable", true);
		folder.setTargetable(targetable);

		final String id = nodeValue(structureJson, "id", null);
		if( id == null && targetable )
		{
			throw new RuntimeException("Each targetable folder must have an 'id' field.");
		}
		folder.setId(id);

		final boolean defaultFolder = nodeValue(structureJson, "selected", false);
		folder.setDefaultFolder(defaultFolder);
		if( defaultFolder )
		{
			session.setTargetFolder(id);
		}

		final JsonNode folders = structureJson.get("folders");
		if( folders != null )
		{
			if( !folders.isArray() )
			{
				throw new RuntimeException("'folders' field must be an array.");
			}
			final ArrayNode foldersArray = (ArrayNode) folders;
			for( JsonNode folderJson : foldersArray )
			{
				if( folderJson.isObject() )
				{
					final TargetFolder subfolder = new TargetFolder();
					folder.addFolder(subfolder);
					recurseFolders((ObjectNode) folderJson, subfolder, session);
				}
			}
		}
	}

	@Nullable
	private TargetFolder findFirstTargetable(TargetFolder folder)
	{
		if( folder.isTargetable() )
		{
			return folder;
		}
		else
		{
			for( TargetFolder f2 : folder.getFolders() )
			{
				final TargetFolder target = findFirstTargetable(f2);
				if( target != null )
				{
					return target;
				}
			}
		}
		return null;
	}

	@Override
	public void processResultForSingle(SectionInfo info, SelectionSession session, Map<String, String> params,
		String prefix, IItem<?> item, SelectedResource resource)
	{
		params.put("folder", resource.getKey().getFolderId());
	}

	@Override
	public void processResultForMultiple(SectionInfo info, SelectionSession session, ObjectNode link, IItem<?> item,
		SelectedResource resource)
	{
		link.put("folder", resource.getKey().getFolderId());
	}
}
