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

package com.tle.web.wizard.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.core.item.standard.operations.AbstractStandardWorkflowOperation;
import com.tle.web.wizard.WizardState;

public class WizardStateOperation extends AbstractStandardWorkflowOperation
{
	private WizardState state;

	@AssistedInject
	protected WizardStateOperation(@Assisted WizardState state)
	{
		this.state = state;
	}

	@Override
	public boolean execute()
	{
		Item item = state.getItem();
		params.setItemKey(new ItemId(item.getUuid(), item.getVersion()), 0l);
		params.setItemPack(new ItemPack(item, state.getItemxml(), state.getStagingId()));
		params.setUpdateSecurity(true);
		return false;
	}
}
