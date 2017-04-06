package com.tle.beans.item;

/**
 * @author Nicholas Read
 */
public enum ItemStatus
{
	DRAFT, LIVE, REJECTED, MODERATING, ARCHIVED, SUSPENDED, DELETED, REVIEW, PERSONAL;

	@Override
	public String toString()
	{
		return super.toString().toLowerCase();
	}
}
