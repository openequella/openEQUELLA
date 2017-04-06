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
