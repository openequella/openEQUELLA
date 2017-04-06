package com.tle.common.i18n;

import java.util.TimeZone;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginTracker;

/**
 * Based on the CurrentLocale code
 * 
 * @author aholland
 */
@NonNullByDefault
public final class CurrentTimeZone
{
	@Nullable
	private static AbstractCurrentTimeZone impl;

	public static TimeZone get()
	{
		return getImpl().get();
	}

	public abstract static class AbstractCurrentTimeZone
	{
		public abstract TimeZone get();
	}

	private CurrentTimeZone()
	{
		throw new Error();
	}

	private static synchronized AbstractCurrentTimeZone getImpl()
	{
		if( impl == null )
		{
			impl = new PluginTracker<AbstractCurrentTimeZone>(AbstractPluginService.get(), CurrentTimeZone.class,
				"currentTimeZoneImpl", null).setBeanKey("bean").getBeanList().get(0);
		}
		return impl;
	}

	public static void initialise(AbstractCurrentTimeZone tzImpl)
	{
		impl = tzImpl;
	}
}
