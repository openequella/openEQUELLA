package com.tle.core.externaltools.dao.impl;

import com.google.inject.Singleton;
import com.tle.common.externaltools.entity.ExternalTool;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.externaltools.dao.ExternalToolsDao;
import com.tle.core.guice.Bind;

@Bind(ExternalToolsDao.class)
@Singleton
public class ExternalToolsDaoImpl extends AbstractEntityDaoImpl<ExternalTool> implements ExternalToolsDao
{
	public ExternalToolsDaoImpl()
	{
		super(ExternalTool.class);
	}

	// TODO
}
