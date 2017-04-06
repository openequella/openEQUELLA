package com.tle.core.filesystem;

import com.tle.common.PathUtils;
import com.tle.core.user.CurrentInstitution;

public class LanguagesFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String LANGUAGES_FOLDER = "Languages";

	public LanguagesFile()
	{
		super(CurrentInstitution.get());
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), LANGUAGES_FOLDER);
	}
}
