/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.auditlog.impl;

import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.WebAuthenticationDetails;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.auditlog.AuditLogExtension;
import com.tle.core.auditlog.AuditLogJavaDao;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Generic audit logging service.
 */
@Bind(AuditLogService.class)
@Singleton
@SuppressWarnings("nls")
public class AuditLogServiceImpl implements AuditLogService
{
	private static final String USER_CATEGORY = "USER";
	private static final String ENTITY_CATEGORY = "ENTITY";
	private static final String SEARCH_CATEGORY = "SEARCH";
	private static final String ITEM_CATEGORY = "ITEM";

	private static final String CREATED_TYPE = "CREATED";
	private static final String MODIFIED_TYPE = "MODIFIED";
	private static final String DELETED_TYPE = "DELETED";
	private static final String PURGED_TYPE = "PURGED";
	private static final String CONTENT_VIEWED_TYPE = "CONTENT_VIEWED";
	private static final String SUMMARY_VIEWED_TYPE = "SUMMARY_VIEWED";

	private static final String SEARCH_FEDERATED_TYPE = "FEDERATED";

	private static final String TRUNCED = "...";

	private PluginTracker<AuditLogExtension> extensionTracker;

	@Override
	@Transactional
	public void removeOldLogs(int daysOld)
	{
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);
		c.set(Calendar.MINUTE, 59);
		c.add(Calendar.DAY_OF_YEAR, -daysOld);

		Date date = c.getTime();
		AuditLogJavaDao.removeEntriesBeforeDate(date);
		for( AuditLogExtension extension : getExtensions() )
		{
			extension.getDao().removeEntriesBeforeDate(date);
		}
	}

	private void logUserEvent(String type, UserState us, HttpServletRequest request)
	{
		UserBean ub = us.getUserBean();
		AuditLogJavaDao.logWithRequest(ub.getUniqueID(), us.getSessionID(), USER_CATEGORY, type, us.getIpAddress(), ub.getUniqueID(),
			ub.getUsername(), us.getTokenSecretId(), us.getInstitution(), request);
	}

	@Override
	@Transactional
	public void logUserLoggedIn(UserState us, HttpServletRequest request)
	{
		logUserEvent("LOGIN", us, request);
	}

	@Override
	@Transactional
	public void logUserFailedAuthentication(String username, WebAuthenticationDetails wad)
	{
		logGeneric(USER_CATEGORY, "AUTH ERROR", wad.getIpAddress(), username, "BAD CREDENTIALS", null);
	}

	@Override
	@Transactional
	public void logUserLoggedOut(UserState us, HttpServletRequest request)
	{
		logUserEvent("LOGOUT", us, request);
	}

	@Override
	@Transactional
	public void logEntityCreated(long entityId)
	{
		logEntityGeneric(CREATED_TYPE, entityId);
	}

	@Override
	@Transactional
	public void logEntityModified(long entityId)
	{
		logEntityGeneric(MODIFIED_TYPE, entityId);
	}

	@Override
	@Transactional
	public void logEntityDeleted(long entityId)
	{
		logEntityGeneric(DELETED_TYPE, entityId);
	}

	@Override
	@Transactional
	public void logObjectDeleted(long objectId, String friendlyName)
	{
		logGeneric(friendlyName, DELETED_TYPE, CurrentUser.getUserID(), Long.toString(objectId), null, null);
	}

	@Override
	@Transactional
	public void logSummaryViewed(String category, ItemKey item, HttpServletRequest request)
	{
		AuditLogJavaDao.logHttp(category, SUMMARY_VIEWED_TYPE, item.getUuid(), Integer.toString(item.getVersion()), null, null, request);
	}

	@Override
	@Transactional
	public void logItemSummaryViewed(Item item, HttpServletRequest request)
	{
		logSummaryViewed(ITEM_CATEGORY, item.getItemId(), request);
	}

	@Override
	@Transactional
	public void logContentViewed(String category, ItemKey itemId, String contentType, String path, HttpServletRequest request)
	{
		AuditLogJavaDao.logHttp(category, CONTENT_VIEWED_TYPE, itemId.getUuid(), Integer.toString(itemId.getVersion()), contentType,
			path, request);
	}

	@Override
	@Transactional
	public void logItemContentViewed(ItemKey itemId, String contentType, String path, Attachment attachment, HttpServletRequest request)
	{
		logContentViewed(ITEM_CATEGORY, itemId, contentType, path, request);
	}

	@Override
	@Transactional
	public void logItemPurged(Item item)
	{
		logGeneric(ITEM_CATEGORY, PURGED_TYPE, item.getUuid(), Integer.toString(item.getVersion()), null, null);
	}

	private void logEntityGeneric(String type, long entityId)
	{
		logGeneric(ENTITY_CATEGORY, type, CurrentUser.getUserID(), Long.toString(entityId), null, null);
	}

	@Override
	@Transactional
	public void logSearch(String type, String freeText, String within, long resultCount)
	{
		logGeneric(SEARCH_CATEGORY, type, freeText, within, Long.toString(resultCount), null);
	}

	@Override
	@Transactional
	public void logFederatedSearch(String freeText, String searchId)
	{
		logGeneric(SEARCH_CATEGORY, SEARCH_FEDERATED_TYPE, freeText, searchId, null, null);
	}

	@Override
	@Transactional
	public void logGeneric(String category, String type, String d1, String d2, String d3, String d4)
	{
		log(CurrentUser.getUserID(), CurrentUser.getSessionID(), category, type, d1, d2, d3, d4,
			CurrentInstitution.get());
	}

	private void log(String userId, String sessionId, String category, String type, String d1, String d2, String d3,
		String d4, Institution institution)
	{
		AuditLogJavaDao.log(userId, sessionId, category, type, d1, d2, d3, d4, institution);
	}

	@Override
	@Transactional
	public Collection<AuditLogExtension> getExtensions()
	{
		return extensionTracker.getBeanList();
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		extensionTracker = new PluginTracker<AuditLogExtension>(pluginService, "com.tle.core.auditlog", "auditTable", null)
			.setBeanKey("bean");
	}

	@Override
	@Transactional
	public void removeEntriesForInstitution(Institution institution)
	{
		AuditLogJavaDao.removeEntriesForInstitution(institution);
		for( AuditLogExtension extension : getExtensions() )
		{
			extension.getDao().removeEntriesForInstitution(institution);
		}
	}
}
