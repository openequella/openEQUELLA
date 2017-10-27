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
