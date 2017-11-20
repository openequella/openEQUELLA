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

package com.tle.core.item.edit.impl;

import java.io.IOException;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.item.edit.attachment.AbstractCustomAttachmentEditor;
import com.tle.core.item.edit.attachment.ScormAttachmentEditor;
import com.tle.core.services.FileSystemService;

@SuppressWarnings("nls")
@Bind
public class ScormAttachmentEditorImpl extends AbstractCustomAttachmentEditor implements ScormAttachmentEditor
{
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public void editPackageFile(String filename)
	{
		if( hasBeenEdited(customAttachment.getUrl(), filename) )
		{
			customAttachment.setUrl(filename);
			updateFileDetails("_IMS/" + filename);
		}
		else if( changeTracker.isForceFileCheck() )
		{
			updateFileDetails("_IMS/" + filename);
		}
	}

	private void updateFileDetails(String filename)
	{
		if( fileHandle != null && fileSystemService.fileExists(fileHandle, filename) )
		{
			try
			{
				String newMd5 = fileSystemService.getMD5Checksum(fileHandle, filename);
				if( hasBeenEdited(customAttachment.getMd5sum(), newMd5) )
				{
					setSize(fileSystemService.fileLength(fileHandle, filename));
					customAttachment.setMd5sum(newMd5);
				}
			}
			catch( IOException e )
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			setSize(-1L);
			customAttachment.setMd5sum(null);
		}
	}

	private void setSize(long size)
	{
		customAttachment.setData("fileSize", size);
	}

	@Override
	public void editScormVersion(String version)
	{
		editCustomData("SCORM_VERSION", version);
	}

	@Override
	public String getCustomType()
	{
		return "scorm";
	}

}
