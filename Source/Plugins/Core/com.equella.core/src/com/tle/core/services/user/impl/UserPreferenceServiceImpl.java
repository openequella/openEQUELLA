/*
 * Copyright 2017 Apereo
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

package com.tle.core.services.user.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.tle.beans.Institution;
import com.tle.beans.UserPreference;
import com.tle.beans.UserPreference.UserPrefKey;
import com.tle.common.Check;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.core.dao.UserPreferenceDao;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionCache;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.xml.service.XmlService;

@Singleton
@SuppressWarnings("nls")
@Bind(UserPreferenceService.class)
public class UserPreferenceServiceImpl implements UserPreferenceService, UserChangeListener
{
	private static final String NULL_PREF = "";
	private static final String CACHE_SETTINGS = "activecache";

	@Inject
	private UserPreferenceDao userPreferencesDao;
	@Inject
	private XmlService xmlService;

	private InstitutionCache<LoadingCache<String, LoadingCache<String, String>>> cache;

	@Inject
	public void setInstitutionService(InstitutionService service)
	{
		cache = service
			.newInstitutionAwareCache(new CacheLoader<Institution, LoadingCache<String, LoadingCache<String, String>>>()
			{
				@Override
				public LoadingCache<String, LoadingCache<String, String>> load(final Institution institution)
				{
					// User ID to preference key/value
					return CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).softValues()
						.build(new CacheLoader<String, LoadingCache<String, String>>()
					{
						@Override
						public LoadingCache<String, String> load(final String userId)
						{
							// Preference key to value
							return CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).softValues()
								.build(new CacheLoader<String, String>()
							{
								@Override
								public String load(String key)
								{
									Criterion c1 = Restrictions.eq("key.userID", userId);
									Criterion c2 = Restrictions.eq("key.institution", institution.getDatabaseId());
									Criterion c3 = Restrictions.eq("key.preferenceID", key);
									UserPreference pref = userPreferencesDao.findByCriteria(c1, c2, c3);

									String rv = NULL_PREF;
									if( pref != null )
									{
										String data = pref.getData();
										if( data != null )
										{
											rv = data;
										}
									}
									return rv;
								}
							});
						}
					});
				}
			});
	}

	@Override
	public String getPreference(String preferenceID)
	{
		UserState userState = CurrentUser.getUserState();
		if( userState == null )
		{
			return null;
		}
		return getPreferenceForUser(userState.getUserBean().getUniqueID(), preferenceID);
	}

	@Override
	@Transactional
	public String getPreferenceForUser(String userId, String key)
	{
		Institution inst = CurrentInstitution.get();
		String rv = null;
		if( inst != null )
		{
			rv = cache.getCache().getUnchecked(userId).getUnchecked(key);
			if( rv == NULL_PREF ) // NOSONAR - static string instance
			{
				rv = null;
			}
		}
		return rv;
	}

	@Override
	@Transactional
	public void setPreference(String preferenceID, String data)
	{
		final UserState userState = CurrentUser.getUserState();
		final String userId = userState.getUserBean().getUniqueID();

		UserPrefKey key = new UserPrefKey();
		key.setInstitution(userState.getInstitution());
		key.setPreferenceID(preferenceID);
		key.setUserID(userId);

		UserPreference pref = new UserPreference();
		pref.setKey(key);
		pref.setData(data);

		pref = userPreferencesDao.merge(pref);
		userPreferencesDao.saveOrUpdate(pref);

		cache.getCache().getUnchecked(userId).invalidate(preferenceID);
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		Criterion c1 = Restrictions.eq("key.userID", event.getUserID());
		Criterion c2 = Restrictions.eq("key.institution", CurrentInstitution.get().getDatabaseId());

		for( UserPreference pref : userPreferencesDao.findAllByCriteria(c1, c2) )
		{
			userPreferencesDao.delete(pref);
		}
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Nothing to do here
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		userPreferencesDao.transferUserId(event.getFromUserId(), event.getToUserId());
	}

	@Override
	public boolean isHideLoginNotice()
	{
		return Boolean.valueOf(getPreference(HIDE_LOGIN_NOTICE));
	}

	@Override
	public void setHideLoginNotice(boolean b)
	{
		setPreference(HIDE_LOGIN_NOTICE, Boolean.toString(b));
	}

	@Override
	public String getDateFormat()
	{
		return getPreference(DATEFORMAT);
	}

	@Override
	public void setDateFormat(String dateformat)
	{
		setPreference(DATEFORMAT, dateformat);
	}

	@Override
	public Locale getLocale()
	{
		String data = getPreference(LANGUAGE_CODE);
		if( data == null )
		{
			return null;
		}
		else
		{
			String[] vs = (String[]) xmlService.deserialiseFromXml(getClass().getClassLoader(), data);
			return new Locale(vs[0], vs[1], vs[2]);
		}
	}

	@Override
	public void setLocale(Locale locale)
	{
		String data = null;
		if( locale != null )
		{
			String[] vs = new String[3];
			vs[0] = locale.getLanguage();
			vs[1] = locale.getCountry();
			vs[2] = locale.getVariant();
			data = xmlService.serialiseToXml(vs);
		}
		setPreference(LANGUAGE_CODE, data);
	}

	@Override
	public Map<String, String> getPreferenceForAllUsers(String key)
	{
		Map<String, String> prefMap = Maps.newHashMap();
		Criterion c1 = Restrictions.eq("key.preferenceID", key);
		Criterion c2 = Restrictions.eq("key.institution", CurrentInstitution.get().getDatabaseId());
		for( UserPreference pref : userPreferencesDao.findAllByCriteria(c1, c2) )
		{
			prefMap.put(pref.getKey().getUserID(), pref.getData());
		}
		return prefMap;
	}

	@Override
	public Map<String, String> getPreferenceForUsers(String key, Collection<String> users)
	{
		Map<String, String> prefMap = Maps.newHashMap();
		Criterion c1 = Restrictions.eq("key.preferenceID", key);
		Criterion c2 = Restrictions.eq("key.institution", CurrentInstitution.get().getDatabaseId());
		Criterion c3 = Restrictions.in("key.userID", users);
		if (!users.isEmpty())
		{
			for (UserPreference pref : userPreferencesDao.findAllByCriteria(c1, c2, c3))
			{
				prefMap.put(pref.getKey().getUserID(), pref.getData());
			}
		}
		return prefMap;
	}

	@Override
	public PropBagEx getRemoteCachingPreferences()
	{
		String pref = getPreference(CACHE_SETTINGS);
		PropBagEx xml;
		if( pref == null )
		{
			xml = new PropBagEx();
		}
		else
		{
			xml = new PropBagEx(pref);
		}
		return xml;
	}

	@Override
	public void setRemoteCachingPreferences(PropBagEx xml)
	{
		setPreference(CACHE_SETTINGS, xml.toString());
	}

	@Override
	public TimeZone getTimeZone()
	{
		String timezone = getPreference(TIMEZONE);
		if( timezone == null || timezone.equals(Constants.BLANK) )
		{
			return null;
		}
		return TimeZone.getTimeZone(timezone);
	}

	@Override
	public void setTimeZone(String timeZoneId)
	{
		TimeZone timezone;
		if( timeZoneId.equals("") )
		{
			setPreference(TIMEZONE, timeZoneId);
		}
		else
		{
			timezone = TimeZone.getTimeZone(timeZoneId);
			setPreference(TIMEZONE, timezone.getID());
		}
	}

	@Transactional
	@Override
	public Set<String> getReferencedUsers()
	{
		return userPreferencesDao.getReferencedUsers();
	}

	@Override
	public Locale getPreferredLocale(HttpServletRequest request)
	{
		Locale locale = null;
		if( CurrentInstitution.get() != null && !CurrentUser.isGuest() && !CurrentUser.getUserState().isSystem() )
		{
			// Is locale selected as a user preference?
			locale = getLocale();
		}
		if( locale == null && request != null )
		{
			// Is it part of the browser request?
			locale = request.getLocale();
		}
		if( locale == null )
		{
			return Locale.getDefault();
		}
		return locale;
	}

	@Override
	public TimeZone getPreferredTimeZone(TimeZone defaultTimezone)
	{
		TimeZone tz = null;
		if( CurrentInstitution.get() != null && !CurrentUser.isGuest() && !CurrentUser.getUserState().isSystem() )
		{
			tz = getTimeZone();
		}
		if( tz == null )
		{
			Institution institution = CurrentInstitution.get();
			if( institution != null )
			{
				String tzId = institution.getTimeZone();
				if( !Check.isEmpty(tzId) )
				{
					return TimeZone.getTimeZone(tzId);
				}
				else
				{
					return defaultTimezone;
				}
			}
			else
			{
				return defaultTimezone;
			}
		}
		return tz;
	}

	@Override
	public boolean isSearchAttachment()
	{
		String preference = getPreference(SEARCH_WITHIN_ATTACHMENT);
		if( preference == null )
		{
			return true;
		}

		return Boolean.valueOf(preference);
	}

	@Override
	public void setSearchAttachment(boolean b)
	{
		setPreference(SEARCH_WITHIN_ATTACHMENT, Boolean.toString(b));
	}
}
