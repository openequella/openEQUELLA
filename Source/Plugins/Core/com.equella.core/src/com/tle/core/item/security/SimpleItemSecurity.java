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

package com.tle.core.item.security;

import java.util.Collection;

public class SimpleItemSecurity
{
	private final long itemId;
	private final String status;
	private final long collectionId;
	private final Collection<String> metadataTargets;
	private final boolean owner;

	public SimpleItemSecurity(long itemId, String status, long collectionId, Collection<String> metadataTargets,
		boolean owner)
	{
		this.itemId = itemId;
		this.status = status;
		this.collectionId = collectionId;
		this.metadataTargets = metadataTargets;
		this.owner = owner;
	}

	public long getItemId()
	{
		return itemId;
	}

	public String getStatus()
	{
		return status;
	}

	public long getCollectionId()
	{
		return collectionId;
	}

	public Collection<String> getMetadataTargets()
	{
		return metadataTargets;
	}

	public boolean isOwner()
	{
		return owner;
	}

}
