package com.tle.core.workflow.thumbnail.dao;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemKey;
import com.tle.core.dao.ItemDaoExtension;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public interface ThumbnailRequestDao extends GenericInstitutionalDao<ThumbnailRequest, Long>, ItemDaoExtension
{
	List<ThumbnailRequest> list(Institution institution);

	List<ThumbnailRequest> list(Institution institution, ItemKey itemId);

	List<ThumbnailRequest> listForFile(Institution institution, ItemKey itemId, String filenameHash);

	List<ThumbnailRequest> listForHandle(Institution institution, ItemKey itemId, String serialHandle);

	@Nullable
	ThumbnailRequest getByUuid(String requestUuid);

	boolean exists(ItemKey itemId, String serialHandle, String filename, String filenameHash);
}
