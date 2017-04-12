package com.tle.core.item.serializer;

import com.tle.beans.item.ItemIdKey;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

public interface ItemDeserializerService
{
	/**
	 * @param itemBean
	 * @param stagingUuid
	 * @param lockId
	 * @param unlock
	 * @param ensureOnIndexList
	 * @return
	 */
	ItemIdKey edit(EquellaItemBean itemBean, String stagingUuid, String lockId, boolean unlock,
		boolean ensureOnIndexList);

	/**
	 * @param equellaItemBean
	 * @param stagingUuid
	 * @param dontSubmit
	 * @param ensureOnIndexList
	 * @return
	 */
	ItemIdKey newItem(EquellaItemBean equellaItemBean, String stagingUuid, boolean dontSubmit,
		boolean ensureOnIndexList, boolean noAutoArchive);

	/**
	 * @param equellaItemBean
	 * @param stagingUuid
	 * @param dontSubmit
	 * @param ensureOnIndexList
	 * @return
	 */
	ItemIdKey importItem(EquellaItemBean equellaItemBean, String stagingUuid, boolean ensureOnIndexList);
}
