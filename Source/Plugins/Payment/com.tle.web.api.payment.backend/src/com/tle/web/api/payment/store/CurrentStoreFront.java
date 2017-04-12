package com.tle.web.api.payment.store;

import com.tle.common.payment.entity.StoreFront;

/**
 * @author Aaron
 */
public final class CurrentStoreFront
{
	private static ThreadLocal<StoreFront> stateLocal = new ThreadLocal<StoreFront>();

	private CurrentStoreFront()
	{
		throw new Error();
	}

	public static StoreFront get()
	{
		return stateLocal.get();
	}

	public static void set(StoreFront sf)
	{
		stateLocal.set(sf);
	}
}
