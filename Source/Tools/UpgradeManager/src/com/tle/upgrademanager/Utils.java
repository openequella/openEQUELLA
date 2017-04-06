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
