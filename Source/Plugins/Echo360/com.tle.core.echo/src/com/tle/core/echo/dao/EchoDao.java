package com.tle.core.echo.dao;

import com.tle.beans.Institution;
import com.tle.core.dao.AbstractEntityDao;
import com.tle.core.echo.entity.EchoServer;

public interface EchoDao extends AbstractEntityDao<EchoServer>
{
	EchoServer getBySystemID(Institution inst, String esid);
}
