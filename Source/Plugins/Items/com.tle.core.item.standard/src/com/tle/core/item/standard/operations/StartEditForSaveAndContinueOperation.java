package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.security.SecurityConstants;
import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = SecurityConstants.EDIT_ITEM)
public class StartEditForSaveAndContinueOperation extends StartLockOperation
{
	private String stagingUuid;

	@AssistedInject
	protected StartEditForSaveAndContinueOperation(@Assisted String stagingUuid)
	{
		super(true);
		this.stagingUuid = stagingUuid;
	}

	@Override
	public boolean execute()
	{
		boolean mod = super.execute();
		getItemPack().setStagingID(stagingUuid);
		return mod;
	}
}
