package com.tle.core.workflow.thumbnail;

/**
 * @author Aaron
 *
 */
public enum ThumbnailType
{
	TYPE_STANDARD_THUMB(1), TYPE_GALLERY_THUMB(2), TYPE_GALLERY_PREVIEW(4);

	private final int value;

	private ThumbnailType(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	public boolean enabled(int flags)
	{
		return (flags & value) != 0;
	}
}
