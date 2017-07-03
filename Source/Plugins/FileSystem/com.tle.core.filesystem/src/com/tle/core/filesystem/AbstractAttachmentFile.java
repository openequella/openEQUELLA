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
