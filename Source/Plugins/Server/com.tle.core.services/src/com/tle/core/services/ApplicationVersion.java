package com.tle.core.services;

import com.dytech.edge.common.Version;

public final class ApplicationVersion
{
	private final static Version version = Version.load();

	private ApplicationVersion()
	{
		throw new Error();
	}

	public static Version get()
	{
		return version;
	}
}
