package com.tle.core.cloud.service;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.cloud.beans.converted.CloudAttachment;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.cloud.search.CloudSearch;
import com.tle.core.cloud.search.filter.CloudFilterInfo;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface CloudService
{
	/**
	 * @return true if equella can reach the interwebs and admin hasn't turned
	 *         off cloud searching
	 */
	boolean isCloudy();

	int resultCount(String query);

	CloudSearchResults search(CloudSearch search, int offset, int count);

	CloudFilterInfo getCloudFilterInfo();

	@Nullable
	CloudItem getItem(String uuid, int version);

	int getLiveItemVersion(String uuid);

	MimeEntry getMimeType(CloudAttachment cloudAttachment);
}
