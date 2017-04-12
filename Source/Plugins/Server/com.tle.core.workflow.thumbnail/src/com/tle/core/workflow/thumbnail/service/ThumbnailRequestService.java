package com.tle.core.workflow.thumbnail.service;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.ItemKey;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;

/**
 * Do not use directly. You should invoke methods on the ThumbnailService.
 * 
 * @author Aaron
 *
 */
@NonNullByDefault
public interface ThumbnailRequestService
{
	void newRequest(String filename, ItemKey itemId, FileHandle handle, int thumbFlags, boolean forceIt,
		boolean clearPending);

	void update(ThumbnailRequest thumbnailRequest);

	/**
	 * This is deliberately just the UUID. It will try to load the request again, which may already be gone.
	 * @param requestUuid
	 */
	void delete(String requestUuid);

	List<ThumbnailRequest> listForHandle(ItemKey itemId, FileHandle handle);

	/**
	 * 
	 * @param itemId
	 * @param excludingHandle List files that <em>don't</em> belong to this handle.
	 * @param filename
	 * @return
	 */
	List<ThumbnailRequest> listForFile(ItemKey itemId, FileHandle excludingHandle, String filename);

	List<ThumbnailRequest> list(Institution institution);

	List<ThumbnailRequest> list(Institution institution, ItemKey itemId);

	@Nullable
	ThumbnailRequest getByUuid(String requestUuid);

	boolean exists(ItemKey itemId, FileHandle handle, String filename);
}
