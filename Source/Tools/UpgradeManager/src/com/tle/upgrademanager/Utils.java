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

package com.tle.upgrademanager;

import java.util.Comparator;
import java.util.regex.Pattern;

import com.dytech.common.text.NumberStringComparator;
import com.tle.upgrademanager.handlers.PagesHandler.WebVersion;

public final class Utils
{
	public static final Pattern VERSION_EXTRACT = Pattern
		.compile("^tle-upgrade-(\\d+\\.\\d+\\.r\\d+) \\((.+)\\)\\.zip$"); //$NON-NLS-1$

	public static final Comparator<WebVersion> VERSION_COMPARATOR = new InverseComparator<WebVersion>(
		new NumberStringComparator<WebVersion>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String convertToString(WebVersion wv)
			{
				return wv.getMmr();
			}
		});

	public static final String UNKNOWN_VERSION = "Unknown"; //$NON-NLS-1$
	public static final String EQUELLASERVER_DIR = "server"; //$NON-NLS-1$

	public static final String DEBUG_FLAG = "DEBUG"; //$NON-NLS-1$

	private Utils()
	{
		throw new Error();
	}
}
