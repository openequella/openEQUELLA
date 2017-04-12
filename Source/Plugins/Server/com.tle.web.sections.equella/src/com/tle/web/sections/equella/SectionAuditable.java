package com.tle.web.sections.equella;

/**
 * @author aholland
 */
public interface SectionAuditable
{
	public static enum AuditLevel
	{
		NONE, NORMAL, SMART
	}

	void setAuditLevelString(String auditLevelString);
}
