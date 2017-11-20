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

package com.dytech.edge.exceptions;

import com.tle.beans.item.ItemKey;
import com.tle.common.beans.exception.NotFoundException;

/**
 * @author aholland
 */
public class ItemNotFoundException extends NotFoundException
{
	private static final long serialVersionUID = 1L;

	private final ItemKey itemId;

	public ItemNotFoundException(ItemKey itemId)
	{
		this(itemId, false);
	}

	@SuppressWarnings("nls")
	public ItemNotFoundException(ItemKey itemId, boolean fromRequest)
	{
		super("Item not found " + itemId.toString(), fromRequest);
		this.itemId = itemId;
	}

	public ItemKey getItemId()
	{
		return itemId;
	}
}
