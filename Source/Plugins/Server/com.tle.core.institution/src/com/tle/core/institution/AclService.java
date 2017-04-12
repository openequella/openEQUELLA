package com.tle.core.institution;

import java.util.List;

import com.tle.beans.security.AccessEntry;

/**
 * Service to call on acl manipulations for REST api
 * 
 * @author larry
 */
public interface AclService
{
	List<AccessEntry> listAll();

	void saveAll(List<AccessEntry> acls);
}
