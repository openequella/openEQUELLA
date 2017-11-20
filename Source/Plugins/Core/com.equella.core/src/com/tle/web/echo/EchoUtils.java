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

package com.tle.web.echo;

import java.text.MessageFormat;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import com.google.common.collect.Lists;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;

@SuppressWarnings("nls")
public final class EchoUtils
{
	public static final List<String> VIEWERS = Lists.newArrayList("echoCenterViewer", "echoPlayerViewer",
		"echoVodcastViewer", "echoPodcastViewer");
	public static final String ATTACHMENT_TYPE = "echo";
	public static final String MIME_TYPE = "equella/attachment-echo";
	public static final String MIME_DESC = "Echo presentation";
	public static final String MIME_ICON_PATH = "icons/echo.png";

	public static final String PROPERTY_ECHO_DATA = "echoData";

	public static final String DEFAULT_SECURITY_REALM = "default";

	private static final PluginResourceHelper RESOURCES = ResourcesService.getResourceHelper(EchoUtils.class);

	// TODO refactor this out to be used elsewhere (eg. Youtube)
	public static String formatDuration(long duration)
	{
		// Using Joda Time
		DateTime now = new DateTime(); // Now
		DateTime plus = now.plus(new Duration(duration * 1000));

		// Define and calculate the interval of time
		Interval interval = new Interval(now.getMillis(), plus.getMillis());
		Period period = interval.toPeriod(PeriodType.time());

		// Define the period formatter for pretty printing
		String ampersand = " & ";
		PeriodFormatter pf = new PeriodFormatterBuilder().appendHours().appendSuffix(ds("hour"), ds("hours"))
			.appendSeparator(" ", ampersand).appendMinutes().appendSuffix(ds("minute"), ds("minutes"))
			.appendSeparator(ampersand).appendSeconds().appendSuffix(ds("second"), ds("seconds")).toFormatter();

		return pf.print(period).trim();
	}

	private EchoUtils()
	{
		throw new Error();
	}

	public static String ds(String key)
	{
		return MessageFormat.format(" {0} ", CurrentLocale.get(RESOURCES.key("duration." + key)));
	}
}
