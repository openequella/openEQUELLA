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

package com.tle.common.wizard.controls.htmleditmce;

import java.util.Set;

import com.dytech.edge.wizard.beans.control.CustomControl;

@SuppressWarnings("nls")
public class HtmlEditMceControl extends CustomControl
{
	private static final long serialVersionUID = -1129109182851406172L;

	private static final String LAZYLOAD = "LAZYLOAD";

	/**
	 * Controlling boolean for collections
	 */
	private static final String SELECTED_COLLECTIONS = "SELECTED_COLLECTIONS";
	/**
	 * The collection Uuids stored & retrieved
	 */
	public static final String SELECTED_COLLECTIONS_UUIDS = "SELECTED_COLLECTIONS_UUIDS";

	/**
	 * Controlling boolean for collections
	 */
	private static final String SELECTED_DYNACOLLS = "SELECTED_DYNACOLLS";
	/**
	 * The dynamic collection Uuids stored & retrieved
	 */
	public static final String SELECTED_DYNACOLL_UUIDS = "SELECTED_DYNACOLL_UUIDS";

	/**
	 * Controlling boolean for collections
	 */
	private static final String SELECTED_SEARCHES = "SELECTED_SEARCHES";
	/**
	 * The PowerSearch Uuids stored & retrieved
	 */
	public static final String SELECTED_SEARCH_UUIDS = "SELECTED_SEARCH_UUIDS";

	/**
	 * Controlling boolean for collections
	 */
	private static final String SELECTED_CONTRIBUTABLES = "SELECTED_CONTRIBUTABLES";
	/**
	 * The contributable collection Uuids stored & retrieved
	 */
	public static final String SELECTED_CONTRIBUTABLE_UUIDS = "SELECTED_CONTRIBUTABLE_UUIDS";

	/**
	 * The RemoteRepos Uuids stored & retrieved. (unused)
	 */
	public static final String SELECTED_REMOTEREPO_UUIDS = "SELECTED_REMOTEREPO_UUIDS";

	public HtmlEditMceControl()
	{
		setClassType("htmleditor");
	}

	public HtmlEditMceControl(CustomControl cloneFrom)
	{
		cloneFrom.cloneTo(this);
	}

	public boolean isLazyLoad()
	{
		return getBooleanAttribute(LAZYLOAD);
	}

	public void setLazyLoad(boolean lazyLoad)
	{
		getAttributes().put(LAZYLOAD, lazyLoad);
	}

	// collections
	public boolean isRestrictCollections()
	{
		return getBooleanAttribute(SELECTED_COLLECTIONS);
	}

	public void setRestrictCollections(boolean selectedCollections)
	{
		getAttributes().put(SELECTED_COLLECTIONS, selectedCollections);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getCollectionsUuids()
	{
		return (Set<String>) getAttributes().get(SELECTED_COLLECTIONS_UUIDS);
	}

	@SuppressWarnings("unchecked")
	public Set<String> setCollectionsUuids(Set<String> uuids)
	{
		return (Set<String>) getAttributes().put(SELECTED_COLLECTIONS_UUIDS, uuids);
	}

	// dynamic collections
	public boolean isRestrictDynacolls()
	{
		return getBooleanAttribute(SELECTED_DYNACOLLS);
	}

	public void setRestrictDynacolls(boolean selectedDynacolls)
	{
		getAttributes().put(SELECTED_DYNACOLLS, selectedDynacolls);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getDynaCollectionsUuids()
	{
		return (Set<String>) getAttributes().get(SELECTED_DYNACOLL_UUIDS);
	}

	@SuppressWarnings("unchecked")
	public Set<String> setDynaCollectionsUuids(Set<String> uuids)
	{
		return (Set<String>) getAttributes().put(SELECTED_DYNACOLL_UUIDS, uuids);
	}

	// advanced searches
	public boolean isRestrictSearches()
	{
		return getBooleanAttribute(SELECTED_SEARCHES);
	}

	public void setRestrictSearches(boolean selectedSearches)
	{
		getAttributes().put(SELECTED_SEARCHES, selectedSearches);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getSearchUuids()
	{
		return (Set<String>) getAttributes().get(SELECTED_SEARCH_UUIDS);
	}

	@SuppressWarnings("unchecked")
	public Set<String> setSearchUuids(Set<String> uuids)
	{
		return (Set<String>) getAttributes().put(SELECTED_SEARCH_UUIDS, uuids);
	}

	// contributable collections
	public boolean isRestrictContributables()
	{
		return getBooleanAttribute(SELECTED_CONTRIBUTABLES);
	}

	public void setRestrictContributables(boolean selectedContributables)
	{
		getAttributes().put(SELECTED_CONTRIBUTABLES, selectedContributables);
	}

	@SuppressWarnings("unchecked")
	public Set<String> getContributableUuids()
	{
		return (Set<String>) getAttributes().get(SELECTED_CONTRIBUTABLE_UUIDS);
	}

	@SuppressWarnings("unchecked")
	public Set<String> setContributableUuids(Set<String> uuids)
	{
		return (Set<String>) getAttributes().put(SELECTED_CONTRIBUTABLE_UUIDS, uuids);
	}
}
