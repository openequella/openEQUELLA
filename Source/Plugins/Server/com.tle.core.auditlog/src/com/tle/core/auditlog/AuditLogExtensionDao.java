package com.tle.core.auditlog;

import java.util.Date;

import com.tle.beans.Institution;
import com.tle.beans.audit.AuditLogTable;
import com.tle.core.hibernate.dao.GenericDao;

public interface AuditLogExtensionDao<T extends AuditLogTable> extends GenericDao<T, Long>
{
	void removeEntriesBeforeDate(Date date);

	void removeEntriesForInstitution(Institution institution);
}
