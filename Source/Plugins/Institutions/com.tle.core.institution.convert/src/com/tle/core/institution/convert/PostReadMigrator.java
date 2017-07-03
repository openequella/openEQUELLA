package com.tle.core.institution.convert;

import java.io.IOException;

public interface PostReadMigrator<T>
{
	void migrate(T obj) throws IOException;
}