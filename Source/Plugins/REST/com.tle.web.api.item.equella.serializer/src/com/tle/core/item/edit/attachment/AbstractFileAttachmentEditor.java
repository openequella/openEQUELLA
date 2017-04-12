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
