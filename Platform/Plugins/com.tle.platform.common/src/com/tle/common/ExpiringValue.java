package com.tle.common;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public final class ExpiringValue<T> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long timeout;
	private final T value;

	public static <U> ExpiringValue<U> expireAt(U value, long exactTime)
	{
		return new ExpiringValue<U>(value, exactTime);
	}

	public static <U> ExpiringValue<U> expireAfter(U value, long duration, TimeUnit unit)
	{
		return new ExpiringValue<U>(value, System.currentTimeMillis() + unit.toMillis(duration));
	}

	private ExpiringValue(T value, long expiresAtMillisSinceEpoch)
	{
		this.value = value;
		this.timeout = expiresAtMillisSinceEpoch;
	}

	public boolean isTimedOut()
	{
		return System.currentTimeMillis() > timeout;
	}

	public T getValue()
	{
		return isTimedOut() ? null : value;
	}
}
