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