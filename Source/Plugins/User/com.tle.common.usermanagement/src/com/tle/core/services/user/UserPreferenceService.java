package com.tle.core.services.user;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.dytech.devlib.PropBagEx;

/*
 * @author Nicholas Read
 */
public interface UserPreferenceService
{
	String HIDE_LOGIN_NOTICE = "hide.login.notice";
	String LANGUAGE_CODE = "language.code";
	String TIMEZONE = "timezone";
	String DATEFORMAT = "dateformat";
	String RECENT_SELECTIONS = "recent.selections";
	String SEARCH_WITHIN_ATTACHMENT = "search.within.attachment";
	String NOTIFY_MYLIVE = "notify.mylive";

	boolean isHideLoginNotice();

	String getPreference(String preferenceID);

	void setPreference(String preferenceID, String data);

	void setHideLoginNotice(boolean b);

	PropBagEx getRemoteCachingPreferences();

	void setRemoteCachingPreferences(PropBagEx xml);

	Locale getLocale();

	void setLocale(Locale languageCode);

	TimeZone getTimeZone();

	void setTimeZone(String timeZoneId);

	String getDateFormat();

	void setDateFormat(String dateFormat);

	Set<String> getReferencedUsers();

	Locale getPreferredLocale(HttpServletRequest request);

	TimeZone getPreferredTimeZone(TimeZone defaultTimeZone);

	String getPreferenceForUser(String userId, String key);

	Map<String, String> getPreferenceForAllUsers(String key);

	Map<String, String> getPreferenceForUsers(String key, Collection<String> users);

	boolean isSearchAttachment();

	void setSearchAttachment(boolean b);

}
