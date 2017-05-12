package com.tle.webtests.test.importexport;

import com.tle.webtests.test.AbstractTest;

public abstract class AbstractInstTest extends AbstractTest
{
	protected static final String INSTITUTION_FILE = "institution.tar.gz";

	@Override
	protected boolean isInstitutional()
	{
		return false;
	}
}
