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