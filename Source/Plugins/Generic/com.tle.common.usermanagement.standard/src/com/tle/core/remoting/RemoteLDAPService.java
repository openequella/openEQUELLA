package com.tle.core.remoting;

import java.util.List;

import javax.naming.Name;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.tle.beans.usermanagement.standard.LDAPSettings;

public interface RemoteLDAPService
{
	List<? extends Attribute> getAttributes(LDAPSettings settings, String base, String[] attributes);

	List<SearchResult> search(LDAPSettings settings, String base, String filter, SearchControls ctls);

	List<Name> getBases(LDAPSettings settings);

	List<String> getDNs(LDAPSettings settings);
}