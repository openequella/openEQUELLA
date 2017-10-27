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

package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Institution;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;
import com.tle.common.filesystem.FileSystemHelper;

/**
 * @author Aaron
 */
@NonNullByDefault
public class SubEntityFile extends EntityFile
{
	private static final long serialVersionUID = 1L;

	private final String extraPath;
	private final EntityFile parent;

	public SubEntityFile(EntityFile parent, String extraPath)
	{
		super(parent.id);
		this.parent = parent;
		this.extraPath = FileSystemHelper.encode(extraPath);
		FileHandleUtils.checkPath(extraPath);
	}

	@Override
	public void setInstitution(Institution institution)
	{
		super.setInstitution(institution);
		parent.setInstitution(institution);
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(parent.getAbsolutePath(), extraPath);
	}

	@Override
	public String getMyPathComponent()
	{
		return extraPath;
	}
}