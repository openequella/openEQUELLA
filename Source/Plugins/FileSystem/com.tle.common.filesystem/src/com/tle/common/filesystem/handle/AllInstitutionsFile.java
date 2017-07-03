package com.tle.common.filesystem.handle;

import com.tle.annotation.NonNullByDefault;

@SuppressWarnings("nls")
@NonNullByDefault
public class AllInstitutionsFile extends AbstractRootFile
{
	private static final long serialVersionUID = 1L;

	private static final String INSTITUTIONS_FOLDER = "Institutions";

	public AllInstitutionsFile()
	{
		super(INSTITUTIONS_FOLDER);
	}
}
