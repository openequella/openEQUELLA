package com.tle.core.item.standard.service;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemId;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public interface ItemStandardService
{
	/**
	 * Used only by REST endpoint.  Should move elsewhere?
	 * 
	 * @param itemId
	 * @param purge
	 * @param waitForIndex
	 */
	void delete(ItemId itemId, boolean purge, boolean waitForIndex, boolean purgeIfDeleted);
}
