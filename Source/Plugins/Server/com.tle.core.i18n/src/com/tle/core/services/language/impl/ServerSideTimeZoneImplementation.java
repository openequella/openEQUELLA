package com.tle.core.services.language.impl;

import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.dytech.edge.web.WebConstants;
import com.google.inject.Singleton;
import com.tle.common.i18n.CurrentTimeZone.AbstractCurrentTimeZone;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;

@Bind
@Singleton
public class ServerSideTimeZoneImplementation extends AbstractCurrentTimeZone
{
	private static final InheritableThreadLocal<TimeZoneData> local = new InheritableThreadLocal<TimeZoneData>();

	@Inject
	private UserPreferenceService userPreferenceService;
	@Inject
	private UserSessionService userSessionService;
	@com.google.inject.Inject(optional = true)
	// can be overrode by the optional-config.properties
	@Named("timeZone.default")
	private String defaultTimeZoneName;
	private TimeZone defaultTimeZone;

	@Override
	public TimeZone get()
	{
		return getTimeZoneData().getTimeZone();
	}

	private TimeZoneData getTimeZoneData()
	{
		TimeZoneData timeZoneData = local.get();
		UserState userState = CurrentUser.getUserState();
		if( timeZoneData != null && timeZoneData.getUserState() == userState )
		{
			return timeZoneData;
		}
		TimeZone tz = null;
		boolean sessionAvailable = userSessionService.isSessionAvailable();
		if( sessionAvailable )
		{
			tz = userSessionService.getAttribute(WebConstants.KEY_TIMEZONE);
		}
		if( tz == null )
		{
			if( userState == null )
			{
				tz = defaultTimeZone;
			}
			else
			{
				tz = userPreferenceService.getPreferredTimeZone(defaultTimeZone);
			}
		}
		if( sessionAvailable )
		{
			userSessionService.setAttribute(WebConstants.KEY_TIMEZONE, tz);
		}
		timeZoneData = new TimeZoneData(userState, tz);
		local.set(timeZoneData);
		return timeZoneData;
	}

	@PostConstruct
	public void setupDefaultTimezone()
	{
		if( defaultTimeZoneName != null )
		{
			defaultTimeZone = TimeZone.getTimeZone(defaultTimeZoneName);
		}
		else
		{
			defaultTimeZone = TimeZone.getDefault();
		}
	}

	private static class TimeZoneData
	{
		private final UserState userState;
		private final TimeZone timeZone;

		public TimeZoneData(UserState userState, TimeZone timeZone)
		{
			this.userState = userState;
			this.timeZone = timeZone;
		}

		public UserState getUserState()
		{
			return userState;
		}

		public TimeZone getTimeZone()
		{
			return timeZone;
		}
	}

	public void clearThreadLocals()
	{
		local.remove();
	}

}
