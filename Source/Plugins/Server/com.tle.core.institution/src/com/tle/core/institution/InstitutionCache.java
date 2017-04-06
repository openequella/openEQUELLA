package com.tle.core.institution;

import com.tle.beans.Institution;

public interface InstitutionCache<T>
{
	T getCache();

	T getCache(Institution inst);

	void clear();

	void clear(Institution institution);
}