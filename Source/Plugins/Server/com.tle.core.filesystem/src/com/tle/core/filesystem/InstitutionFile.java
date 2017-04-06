package com.tle.core.filesystem;

import com.tle.beans.Institution;
import com.tle.common.Check;
import com.tle.common.PathUtils;

public class InstitutionFile extends AllInstitutionsFile
{
	private static final long serialVersionUID = 1L;

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

	public Institution getInstitution()
	{
		return institution;
	}

	@Override
	public String getAbsolutePath()
	{
		return PathUtils.filePath(super.getAbsolutePath(), institution.getFilestoreId());
	}
}
