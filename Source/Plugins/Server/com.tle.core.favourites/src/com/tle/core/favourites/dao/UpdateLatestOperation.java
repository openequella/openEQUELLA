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

package com.tle.core.favourites.dao;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.core.guice.Bind;
import com.tle.core.item.event.IndexItemBackgroundEvent;
import com.tle.core.item.operations.AbstractWorkflowOperation;

@Bind
public class UpdateLatestOperation extends AbstractWorkflowOperation
{
	@Inject
	private BookmarkDao dao;

	@Override
	public boolean execute()
	{
		if( params.isWentLive() )
		{
			List<Item> itemsToUpdate = dao.updateAlwaysLatest(getItem());
			for( Item item : itemsToUpdate )
			{
				addAfterCommitEvent(new IndexItemBackgroundEvent(new ItemIdKey(item), true));
			}
		}
		return false;
	}

}
