package com.tle.core.workflow.operations;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;

// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class ModifyCollaboratorsOperation extends AbstractWorkflowOperation // NOSONAR
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
