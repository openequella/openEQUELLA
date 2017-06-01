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

import com.tle.beans.filesystem.FileHandle;
import com.tle.common.PathUtils;

/**
 * @author aholland
 */
public abstract class AbstractFile implements FileHandle
{
	private static final long serialVersionUID = 1L;

	/**
	 * E.g. AllStaging, AllExport etc.
	 */
	protected final FileHandle parent;
	protected final String name;

	/**
	 * @param root Usually an AllXxxFile
	 * @param name
	 */
	protected AbstractFile(final FileHandle parent, final String name)
	{
		this.parent = parent;
		this.name = name;

		if( parent == null )
		{
			throw new IllegalArgumentException("A parent handle must be specified");
		}

		FileHandleUtils.checkPath(name);
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(parent.getAbsolutePath(), getMyPathComponent());
	}

	@Override
	public String getMyPathComponent()
	{
		return name;
	}

	@Override
	public final String toString()
	{
		return getAbsolutePath();
	}
}
