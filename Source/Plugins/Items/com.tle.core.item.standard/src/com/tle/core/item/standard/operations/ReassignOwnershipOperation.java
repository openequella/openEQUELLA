/*
 * Created on Jul 12, 2004 For "The Learning Edge"
 */
package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author Nicholas Read
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
@SecureOnCall(priv = "REASSIGN_OWNERSHIP_ITEM")
public final class ReassignOwnershipOperation extends AbstractStandardWorkflowOperation // NOSONAR
{
	private final String newOwnerID;
	private String lastReassignedName;

	@AssistedInject
	private ReassignOwnershipOperation(@Assisted("toOwner") String toOwner)
	{
		this.newOwnerID = toOwner;
	}

	public String getLastReassignedName()
	{
		return lastReassignedName;
	}

	@Override
	public boolean execute()
	{
		boolean needsModifying = !getItem().getOwner().equals(newOwnerID);
		if( needsModifying )
		{
			getItem().setOwner(newOwnerID);
			lastReassignedName = CurrentLocale.get(getItem().getName());
			params.setUpdateSecurity(true);
		}
		return needsModifying;
	}
}
