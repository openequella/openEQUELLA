package com.tle.core.filesystem;

import com.tle.beans.Institution;
import com.tle.common.PathUtils;

public class SubItemFile extends ItemFile
{
	private static final long serialVersionUID = 1L;
	private final ItemFile parent;
	private final String extraPath;

	public SubItemFile(ItemFile parent, String extraPath)
	{
		super(parent.getUuid(), parent.getVersion());
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
	public String getAbsolutePath()
	{
		return PathUtils.filePath(parent.getAbsolutePath(), getMyPathComponent());
	}

	@Override
	public String getMyPathComponent()
	{
		return extraPath;
	}
}
