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
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.item.event.IndexItemBackgroundEvent;
import com.tle.core.item.event.IndexItemNowEvent;
import com.tle.core.item.event.WaitForItemIndexEvent;

public class ReindexOnlyOperation extends AbstractStandardWorkflowOperation
{
	private final boolean wait;

	@AssistedInject
	protected ReindexOnlyOperation(@Assisted("wait") boolean wait)
	{
		this.wait = wait;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		item.setDateForIndex(params.getDateNow());
		ItemIdKey idKey = params.getItemIdKey();
		addAfterCommitEvent(new IndexItemNowEvent(idKey));
		addAfterCommitEvent(new IndexItemBackgroundEvent(idKey, false));
		if( wait )
		{
			addAfterCommitEvent(new WaitForItemIndexEvent(idKey));
		}
		return false;
	}

}
