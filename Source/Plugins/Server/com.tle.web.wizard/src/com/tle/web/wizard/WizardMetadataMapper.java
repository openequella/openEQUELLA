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

package com.tle.web.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.base.Throwables;

public class WizardMetadataMapper implements Serializable, Cloneable
{
	private static final long serialVersionUID = 1L;

	private boolean mapNow;
	private String packageExtractedFolder;
	private final List<String> htmlMappedFiles = new ArrayList<String>();

	public String getPackageExtractedFolder()
	{
		return packageExtractedFolder;
	}

	public void setPackageExtractedFolder(String packageExtractedFolder)
	{
		this.packageExtractedFolder = packageExtractedFolder;
	}

	public List<String> getHtmlMappedFiles()
	{
		return htmlMappedFiles;
	}

	public void addHtmlMappedFile(String filename)
	{
		htmlMappedFiles.add(filename);
	}

	public void addHtmlMappedFiles(Collection<String> fileNames)
	{
		htmlMappedFiles.addAll(fileNames);
	}

	public boolean isMapNow()
	{
		return mapNow;
	}

	public void setMapNow(boolean mapNow)
	{
		this.mapNow = mapNow;
	}

	@Override
	protected WizardMetadataMapper clone()
	{
		try
		{
			return (WizardMetadataMapper) super.clone();
		}
		catch( CloneNotSupportedException e )
		{
			throw Throwables.propagate(e);
		}
	}
}
