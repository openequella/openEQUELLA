/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.common.security.remoting;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.tle.beans.security.ACLEntryMapping;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;

public interface RemoteTLEAclManager
{
	/**
	 * Gets a list of ACL details for a given target.
	 */
	TargetList getTargetList(Node privilegeNode, Object domainObj);

	/**
	 * Performs <code>setTargetList(Node, Object, TargetList)</code>, but then
	 * shoots off any necessary item reindexing events. This is pretty much for
	 * use by the Admin Console. This version of the method is invoked by Java
	 * 1.4 clients with no support for Enums.
	 */
	void setTargetListAndReindex(String privilegeNodeValue, Object domainObj, TargetList targetList);

	/**
	 * Filters out privileges that the user does not have for any domain object.
	 */
	Set<String> filterNonGrantedPrivileges(Collection<String> privileges);

	/**
	 * Filters out privileges that the user does not have for any domain object.
	 */
	Set<String> filterNonGrantedPrivileges(String... privileges);

	/**
	 * Filters out privileges that the user does not have for the given domain
	 * object.
	 */
	Set<String> filterNonGrantedPrivileges(Object domainObj, String... privileges);

	/**
	 * Filters out privileges that the user does not have for the given domain
	 * object.
	 */
	Set<String> filterNonGrantedPrivileges(Object domainObj, Collection<String> privileges);

	/**
	 * Returns a list of entries for a domain object.
	 */
	List<ACLEntryMapping> getAllEntriesForObject(Object domainObj, String privilege);

	/**
	 * Returns a list of entries that apply to a domain object, except for those
	 * applied directly to it!
	 */
	List<ACLEntryMapping> getAllEntriesForObjectOtherThanTheObject(Object domainObj, String privilege);
}
