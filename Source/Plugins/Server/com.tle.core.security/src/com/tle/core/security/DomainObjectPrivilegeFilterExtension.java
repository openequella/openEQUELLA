package com.tle.core.security;

import java.util.Map;
import java.util.Set;

/**
 * @author Aaron
 */
public interface DomainObjectPrivilegeFilterExtension
{
	/**
	 * @param domainObject
	 * @param privileges
	 * @return
	 */
	void filterPrivileges(Object domainObject, Set<String> privileges);

	/**
	 * @param objectToPrivileges Map of object to priv/grant
	 */
	<T> void filterPrivileges(Map<T, Map<String, Boolean>> objectToPrivileges);
}
