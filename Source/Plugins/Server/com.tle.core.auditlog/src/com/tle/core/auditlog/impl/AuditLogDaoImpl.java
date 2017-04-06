package com.tle.core.auditlog.impl;

import com.google.inject.Singleton;
import com.tle.beans.audit.AuditLogEntry;
import com.tle.core.auditlog.AuditLogDao;
import com.tle.core.guice.Bind;

@Bind(AuditLogDao.class)
@Singleton
public class AuditLogDaoImpl extends AbstractAuditLogDaoImpl<AuditLogEntry> implements AuditLogDao
{
	public AuditLogDaoImpl()
	{
		super(AuditLogEntry.class);
	}
}
