package com.tle.beans;

import com.google.common.base.Function;

public final class IdTransformer implements Function<IdCloneable, Long>
{
	public static final IdTransformer INSTANCE = new IdTransformer();

	private IdTransformer()
	{
		// don't make more than one
	}

	@Override
	public Long apply(IdCloneable input)
	{
		return input.getId();
	}
}
