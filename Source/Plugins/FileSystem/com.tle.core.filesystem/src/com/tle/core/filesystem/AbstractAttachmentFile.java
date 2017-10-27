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
import com.tle.annotation.Nullable;
import com.tle.common.PathUtils;

@NonNullByDefault
public class AbstractAttachmentFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String ATTACHMENTS_FOLDER = "Attachments";

	@Nullable
	private final String collectionUuid;

	protected AbstractAttachmentFile(@Nullable String collectionUuid)
	{
		this.collectionUuid = collectionUuid;
	}

	@Nullable
	public String getCollectionUuid()
	{
		return collectionUuid;
	}

	@Override
	protected String createAbsolutePath()
	{
		if( collectionUuid == null )
		{
			return PathUtils.filePath(super.createAbsolutePath(), ATTACHMENTS_FOLDER);
		}
		return PathUtils.filePath(super.createAbsolutePath(), ATTACHMENTS_FOLDER, collectionUuid);
	}
}
