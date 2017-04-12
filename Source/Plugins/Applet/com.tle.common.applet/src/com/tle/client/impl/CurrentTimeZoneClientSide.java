package com.tle.client.impl;

import java.util.TimeZone;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.i18n.CurrentTimeZone.AbstractCurrentTimeZone;

/**
 * @author aholland
 */
@NonNullByDefault
public class CurrentTimeZoneClientSide extends AbstractCurrentTimeZone
{
	private final TimeZone zone;

	public CurrentTimeZoneClientSide(TimeZone zone)
	{
		this.zone = zone;
	}

	@Override
	public TimeZone get()
	{
		return zone;
	}
}
