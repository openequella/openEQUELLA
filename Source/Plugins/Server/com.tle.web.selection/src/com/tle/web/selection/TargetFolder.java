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

package com.tle.web.selection;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.Nullable;

/**
 * @author Aaron
 */
public class TargetFolder implements Serializable
{
	private String name;
	private String id;
	private final List<TargetFolder> folders = Lists.newArrayList();
	private final Map<SelectedResourceKey, SelectedResource> selectedResources = new LinkedHashMap<SelectedResourceKey, SelectedResource>();
	private final Map<String, TargetFolder> foldersMap = Maps.newHashMap();
	private boolean targetable;
	private boolean defaultFolder;

	public String getName()
	{
		return name;
	}

	public TargetFolder setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getId()
	{
		return id;
	}

	public TargetFolder setId(String id)
	{
		this.id = id;
		return this;
	}

	public List<TargetFolder> getFolders()
	{
		return Collections.unmodifiableList(folders);
	}

	public boolean hasChildrenFolders()
	{
		return folders.size() > 0;
	}

	@Nullable
	public TargetFolder getFolder(String folderId)
	{
		if( folderId.equals(id) && targetable )
		{
			return this;
		}
		for( TargetFolder f : folders )
		{
			TargetFolder folder = f.getFolder(folderId);
			if( folder != null )
			{
				return folder;
			}
		}
		return null;
	}

	public TargetFolder addFolder(TargetFolder folder)
	{
		foldersMap.put(folder.getId(), folder);
		folders.add(folder);
		return this;
	}

	/**
	 * @return A shallow search for selected resources, ie. only the resources
	 *         in this folder
	 */
	public Map<SelectedResourceKey, SelectedResource> getResources()
	{
		return Collections.unmodifiableMap(selectedResources);
	}

	public int getResourceCount()
	{
		return selectedResources.size();
	}

	/**
	 * @return A deep search for selected resources, ie. resources in this
	 *         folder and a deep search of every sub folder
	 */
	public Map<SelectedResourceKey, SelectedResource> getAllResources()
	{
		Map<SelectedResourceKey, SelectedResource> all = Maps.newHashMap();
		all.putAll(getResources());
		for( TargetFolder folder : folders )
		{
			all.putAll(folder.getAllResources());
		}
		return all;
	}

	public TargetFolder addResource(SelectedResource resource)
	{
		selectedResources.put(resource.getKey(), resource);
		return this;
	}

	public TargetFolder removeResource(SelectedResourceKey resourceKey)
	{
		selectedResources.remove(resourceKey);
		return this;
	}

	/**
	 * Performs a deep search for the selected resource and removes all
	 * instances of it in this folder and subfolders
	 * 
	 * @param resource
	 * @return
	 */
	public TargetFolder removeAllInstancesOfResource(SelectedResourceKey resourceKey)
	{
		selectedResources.remove(resourceKey);
		for( TargetFolder folder : folders )
		{
			folder.removeAllInstancesOfResource(resourceKey);
		}
		return this;
	}

	/**
	 * Clears resources in this direct folder only
	 * 
	 * @return
	 */
	public TargetFolder clearResources()
	{
		selectedResources.clear();
		return this;
	}

	/**
	 * Clears resources in this direct folder and every subfolder of this one
	 * 
	 * @return
	 */
	public TargetFolder clearAllResources()
	{
		clearResources();
		for( TargetFolder f : folders )
		{
			f.clearAllResources();
		}
		return this;
	}

	/**
	 * @return Some folders may only be containers for sub folders. Blackboard's
	 *         courses for example.
	 */
	public boolean isTargetable()
	{
		return targetable;
	}

	public void setTargetable(boolean targetable)
	{
		this.targetable = targetable;
	}

	/**
	 * @return Is a default folder, ie. we came from this folder in the LMS
	 */
	public boolean isDefaultFolder()
	{
		return defaultFolder;
	}

	public void setDefaultFolder(boolean defaultFolder)
	{
		this.defaultFolder = defaultFolder;
	}

	@Override
	public String toString()
	{
		return name + " targetable:" + targetable;
	}
}
