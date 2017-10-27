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

package com.tle.core.item.standard.operations;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class ModifyCollaboratorsOperation extends AbstractStandardWorkflowOperation // NOSONAR
{
	private boolean remove;
	private boolean bulkAdd;
	private String user;
	private Set<String> allCollabs;

	@AssistedInject
	private ModifyCollaboratorsOperation(@Assisted Set<String> allCollabs)
	{
		this.allCollabs = allCollabs;
		this.bulkAdd = true;
	}

	@AssistedInject
	private ModifyCollaboratorsOperation(@Assisted String user, @Assisted boolean remove)
	{
		this.user = user;
		this.remove = remove;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();

		if( bulkAdd )
		{
			Set<String> newAndOld = new HashSet<String>(allCollabs);
			newAndOld.addAll(item.getCollaborators());
			item.setCollaborators(newAndOld);
			params.setUpdateSecurity(true);
			return true;
		}

		// Add or remove a single collaborator
		Set<String> collabs = item.getCollaborators();
		boolean success = remove ? collabs.remove(user) : collabs.add(user);
		params.setUpdateSecurity(success);
		return success;
	}
}
