package com.dytech.edge.exceptions;

import com.tle.beans.item.ItemKey;
import com.tle.common.beans.exception.NotFoundException;

/**
 * @author aholland
 */
public class AttachmentNotFoundException extends NotFoundException
{
	private static final long serialVersionUID = 1L;

	private final ItemKey itemId;
	private final String filename;

	public AttachmentNotFoundException(ItemKey itemId, String filename)
	{
		this(itemId, filename, false);
	}

	@SuppressWarnings("nls")
	public AttachmentNotFoundException(ItemKey itemId, String filename, boolean fromRequest)
	{
		super("Could not find attachment " + filename + " on Item " + itemId.toString(), fromRequest);
		this.itemId = itemId;
		this.filename = filename;

		setShowStackTrace(false);
	}

	public ItemKey getItemId()
	{
		return itemId;
	}

	public String getFilename()
	{
		return filename;
	}
}
