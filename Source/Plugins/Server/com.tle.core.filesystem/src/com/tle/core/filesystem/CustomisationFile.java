package com.tle.core.filesystem;

import com.tle.beans.Institution;
import com.tle.common.PathUtils;
import com.tle.core.user.CurrentInstitution;

public class CustomisationFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String CUSTOM2_FOLDER = "Custom2";

	/**
	 * References the current customisation for the given institution.
	 */
	public CustomisationFile()
	{
		this(CurrentInstitution.get());
	}

	public CustomisationFile(Institution inst)
	{
		super(inst);
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), CUSTOM2_FOLDER);
	}
}
