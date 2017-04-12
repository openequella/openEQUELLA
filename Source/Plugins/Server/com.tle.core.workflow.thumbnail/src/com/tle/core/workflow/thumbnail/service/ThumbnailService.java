package com.tle.core.workflow.thumbnail.service;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemKey;

public interface ThumbnailService
{
	/**
	 * 
	 * @param itemId
	 * @param handle
	 * @param filename
	 * @param forceIt
	 * @param clearPending Only set to true when you are committed to the request AND you are coming from a staging. 
	 * I.e. there is no way you are backing out of this via a user cancel. 
	 * @return
	 */
	String submitThumbnailRequest(ItemKey itemId, FileHandle handle, String filename, boolean forceIt,
		boolean clearPending);

	/**
	 * Cancels all requests relating to the supplied item+handle.  Use in the case of an edit being cancelled.
	 * @param itemId
	 * @param handle
	 */
	void cancelRequests(ItemKey itemId, FileHandle handle);
}
