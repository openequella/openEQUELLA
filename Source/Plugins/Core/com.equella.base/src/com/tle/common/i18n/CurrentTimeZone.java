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
			impl = new PluginTracker<AbstractCurrentTimeZone>(AbstractPluginService.get(), "com.tle.common.i18n",
				"currentTimeZoneImpl", null).setBeanKey("bean").getBeanList().get(0);
		}
		return impl;
	}

	public static void initialise(AbstractCurrentTimeZone tzImpl)
	{
		impl = tzImpl;
	}
}
