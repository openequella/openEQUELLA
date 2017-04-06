package com.tle.core.filesystem;

import com.tle.beans.entity.BaseEntity;
import com.tle.common.PathUtils;

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
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), FileHandleUtils.getHashedPath(id));
	}
}
