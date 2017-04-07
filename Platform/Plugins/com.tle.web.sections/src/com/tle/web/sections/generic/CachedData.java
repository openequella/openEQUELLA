package com.tle.web.sections.generic;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public class CachedData<T>
{
	private boolean cached;
	private T cache;

	@Nullable
	public T getCachedValue()
	{
		if( !cached )
		{
			throw new RuntimeException("Not cached yet"); //$NON-NLS-1$
		}
		return cache;
	}

	@Nullable
	public T get(SectionInfo info, CacheFiller<T> filler)
	{
		if( !cached )
		{
			cache = filler.get(info);
			cached = true;
		}
		return cache;
	}

	@NonNullByDefault
	public interface CacheFiller<T>
	{
		@Nullable
		T get(SectionInfo info);
	}

	public void clear()
	{
		cached = false;
	}
}
