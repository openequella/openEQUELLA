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

package com.tle.web.institution.section;

import java.util.HashMap;
import java.util.Map;

import com.tle.common.FileSizeUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.render.SectionRenderable;

public class EditInstitutionModel
{
	@Bookmarked
	private long id;
	@Bookmarked
	private boolean loaded;
	@Bookmarked
	private boolean navigateAway;
	private String fileSystemUsage;
	private Map<String, String> errors = new HashMap<String, String>();
	private SectionRenderable selectedDatabase;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public boolean hasLoaded()
	{
		return loaded;
	}

	public void setLoaded(boolean loaded)
	{
		this.loaded = loaded;
	}

	public void setErrors(Map<String, String> errors)
	{
		this.errors = errors;
	}

	public Map<String, String> getErrors()
	{
		return errors;
	}

	public SectionRenderable getSelectedDatabase()
	{
		return selectedDatabase;
	}

	public void setSelectedDatabase(SectionRenderable selectedDatabase)
	{
		this.selectedDatabase = selectedDatabase;
	}

	public boolean isNavigateAway()
	{
		return navigateAway;
	}

	public void setNavigateAway(boolean navigateAway)
	{
		this.navigateAway = navigateAway;
	}

	public String getFileSystemUsage()
	{
		return fileSystemUsage;
	}

	public void setFileSystemUsage(String fileSystemUsage)
	{
		this.fileSystemUsage = fileSystemUsage;
	}

	public void setFileSystemUsage(long dbbl)
	{
		this.fileSystemUsage = FileSizeUtils.humanReadableGigabyte(dbbl);
	}

}