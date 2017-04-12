package com.tle.core.quota;

import java.util.List;

import com.dytech.edge.common.valuebean.UserBean;
import com.tle.beans.system.QuotaSettings.UserQuota;

public interface QuotaPolicy
{
	/**
	 * max or sum?
	 */
	long getLimit(List<UserQuota> quotas);

	/**
	 * owner, collab?
	 */
	long calculateUserFileSize(UserBean userBean);
}
