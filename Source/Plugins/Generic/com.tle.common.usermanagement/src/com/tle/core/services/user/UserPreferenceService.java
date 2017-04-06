package com.tle.core.services.user;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import com.dytech.devlib.PropBagEx;

/*
 * @author Nicholas Read
 */
public interface UserPreferenceService
{
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

	boolean isSearchAttachment();

	void setSearchAttachment(boolean b);

}
