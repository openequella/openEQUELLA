package com.dytech.edge.exceptions;

import com.tle.beans.item.ItemKey;

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
