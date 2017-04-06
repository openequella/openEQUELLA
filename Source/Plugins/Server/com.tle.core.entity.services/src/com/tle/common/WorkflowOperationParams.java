package com.tle.common;

/**
 * @author aholland
 */
public interface WorkflowOperationParams
{
	void setAttribute(String name, String value);

	void setSecurityObject(Object object);
}
