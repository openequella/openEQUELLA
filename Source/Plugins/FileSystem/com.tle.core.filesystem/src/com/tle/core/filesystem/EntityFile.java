package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileHandleUtils;

@NonNullByDefault
public class EntityFile extends AbstractTemplateFile
{
	private static final long serialVersionUID = 1L;

	protected final Long id;

	public EntityFile(BaseEntity entity)
	{
		id = entity.getId();
	}

	public EntityFile(long id)
	{
		this.id = id;
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), FileHandleUtils.getHashedPath(id));
	}
}
