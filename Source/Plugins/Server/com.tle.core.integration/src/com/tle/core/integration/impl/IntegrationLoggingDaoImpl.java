package com.tle.core.integration.impl;

import javax.inject.Singleton;

import com.tle.core.auditlog.impl.AbstractAuditLogDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.integration.IntegrationLoggingDao;
import com.tle.core.integration.beans.AuditLogLms;

@Bind(IntegrationLoggingDao.class)
@Singleton
public class IntegrationLoggingDaoImpl extends AbstractAuditLogDaoImpl<AuditLogLms> implements IntegrationLoggingDao
{

	public IntegrationLoggingDaoImpl()
	{
		super(AuditLogLms.class);
	}

}
