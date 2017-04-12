package com.tle.core.auditlog;

import com.tle.beans.audit.AuditLogTable;

public interface AuditLogExtension
{
	AuditLogExtensionDao<? extends AuditLogTable> getDao();

	String getShortName();
}
