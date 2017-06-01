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

package com.tle.core.item.edit.attachment;

import java.io.IOException;

import javax.inject.Inject;

import com.tle.core.services.FileSystemService;

public abstract class AbstractFileAttachmentEditor extends AbstractAttachmentEditor
{
	@Inject
	private FileSystemService fileSystemService;

	protected void updateFileDetails(String filename, boolean filenameChanged)
	{
		if( fileHandle != null && fileSystemService.fileExists(fileHandle, filename) )
		{
			try
			{
				boolean forceThumb = filenameChanged;
				String newMd5 = fileSystemService.getMD5Checksum(fileHandle, filename);
				if( hasBeenEdited(getExistingMd5(), newMd5) )
				{
					forceThumb = true;
					setSize(fileSystemService.fileLength(fileHandle, filename));
					setMd5sum(newMd5);
				}
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			setSize(-1);
			setMd5sum(null);
		}
	}

	protected void generateThumbnail(String filename, boolean force)
	{
		// only file thumbs i believe
	}

	protected abstract void setSize(long size);

	protected void setMd5sum(String md5)
	{
		attachment.setMd5sum(md5);
	}

	protected String getExistingMd5()
	{
		return attachment.getMd5sum();
	}
}
