package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.PathUtils;
import com.tle.common.institution.CurrentInstitution;

@NonNullByDefault
public class LanguagesFile extends InstitutionFile
{
	private static final long serialVersionUID = 1L;

	private static final String LANGUAGES_FOLDER = "Languages";

	public LanguagesFile()
	{
		super(CurrentInstitution.get());
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), LANGUAGES_FOLDER);
	}
}
