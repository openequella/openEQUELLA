package com.tle.beans.audit;

import java.io.Serializable;
import java.util.Date;

import com.tle.beans.Institution;

public interface AuditLogTable extends Serializable
{
	long getId();

	Institution getInstitution();

	Date getTimestamp();

	String getUserId();

	String getSessionId();

	void setInstitution(Institution institution);

	void setId(long id);
}
