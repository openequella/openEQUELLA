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

package com.tle.core.metadata.exiftool.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Functions;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.common.Check;
import com.tle.common.util.ExecUtils;
import com.tle.common.util.ExecUtils.ExecResult;
import com.tle.core.guice.Bind;
import com.tle.core.metadata.MetadataHandler;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ExifTool implements MetadataHandler
{
	private List<String> removeTags = Lists.newArrayList("Directory", "FilePermissions", "ExifToolVersion", "Error",
		"ThumbnailImage", "PhotoshopThumbnail");

	@Inject
	@Named("exiftool.path")
	private String exifToolPath;

	@Override
	public void getMetadata(LoadingCache<String, Map<String, String>> metadata, Attachment a)
	{
		if( Objects.equal(a.getAttachmentType(), AttachmentType.FILE) )
		{
			String filename = a.getUrl();
			getMetadata(metadata, new File(filename));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void getMetadata(LoadingCache<String, Map<String, String>> metadata, File f)
	{
		// You don't even need to touch power lines to die. DON'T DIE!
		if( Check.isEmpty(exifToolPath) || !Files.exists(Paths.get(exifToolPath)) || !f.exists() )
		{
			return;
		}

		List<String> commandOpts = Lists.newArrayList();
		commandOpts.add(exifToolPath);
		commandOpts.add("-g"); // Group (EXIF, XMP etc)
		commandOpts.add("-j"); // JSON output
		commandOpts.add("-q"); // Quiet processing
		commandOpts.add("-sort"); // Alphabetical sort
		commandOpts.add("-struct"); // Expand structs (ewww)
		commandOpts.add("-u"); // Unsupported tags

		// Remove unsafe tags e.g Directory/Permissions etc
		for( String tag : removeTags )
		{
			commandOpts.add("-x");
			commandOpts.add(tag);
		}

		// File to process
		commandOpts.add(f.getAbsolutePath());

		ObjectMapper om = new ObjectMapper();
		List<Map<String, Object>> outputList = Lists.newArrayList();

		try
		{
			ExecResult exec = ExecUtils.exec(commandOpts);
			String stdout = exec.getStdout();
			Map<String, Object> jsonMap = Maps.newHashMap();
			flatten(om.readTree(stdout).get(0), jsonMap, false);
			outputList.add(jsonMap);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}

		// This is a bit dodgical... but seems to be a necessary evil
		for( final Entry<String, Object> objectMap : outputList.get(0).entrySet() )
		{
			if( objectMap.getValue() instanceof Map )
			{
				metadata.put(objectMap.getKey(),
					Maps.transformValues(((Map<String, Object>) objectMap.getValue()), Functions.toStringFunction()));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void flatten(JsonNode rootNode, Map<String, Object> map, boolean tooDeep)
	{
		for( Entry<String, JsonNode> child : Lists.newArrayList(rootNode.fields()) )
		{
			String key = child.getKey();
			JsonNode value = child.getValue();
			if( value.isValueNode() )
			{
				map.put(key, value.asText());
			}
			else if( value.isArray() )
			{
				for( int i = 0; i < value.size(); i++ )
				{
					JsonNode arrVal = value.get(i);
					// Normal array
					if( !arrVal.fields().hasNext() )
					{
						addToMap(map, key, arrVal);
					}
					else
					{
						addNodesToMap(map, key, arrVal);
					}
				}
			}
			else if( value.isObject() )
			{
				if( tooDeep )
				{
					addNodesToMap(map, key, value);
				}
				else
				{
					map.put(key, Maps.newLinkedHashMap());
					flatten(value, (Map<String, Object>) map.get(key), true);
				}
			}
		}
	}

	private void addNodesToMap(Map<String, Object> map, String key, JsonNode parent)
	{
		for( Entry<String, JsonNode> child : Lists.newArrayList(parent.fields()) )
		{
			addToMap(map, key + child.getKey(), child.getValue());
		}
	}

	private void addToMap(Map<String, Object> map, String key, JsonNode node)
	{
		String existing = (String) map.get(key);
		if( existing != null )
		{
			existing = Joiner.on(", ").join(existing, node.asText());
			map.put(key, existing);
		}
		else
		{
			map.put(key, node.asText());
		}
	}
}
