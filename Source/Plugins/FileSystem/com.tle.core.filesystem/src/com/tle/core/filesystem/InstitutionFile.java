package com.tle.core.filesystem;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.AllInstitutionsFile;

@NonNullByDefault
public class InstitutionFile extends AllInstitutionsFile
{
	private static final long serialVersionUID = 1L;

	@Nullable
	private Institution institution;

	public InstitutionFile()
	{
		super();
	}

	public InstitutionFile(Institution institution)
	{
		Check.checkNotNull(institution);
		this.institution = institution;
	}

	public void setInstitution(Institution institution)
	{
		Check.checkNotNull(institution);
		this.institution = institution;
	}

	@Nullable
	public Institution getInstitution()
	{
		return institution;
	}

	@Override
	protected String createAbsolutePath()
	{
		return PathUtils.filePath(super.createAbsolutePath(), institution.getFilestoreId());
	}
}
