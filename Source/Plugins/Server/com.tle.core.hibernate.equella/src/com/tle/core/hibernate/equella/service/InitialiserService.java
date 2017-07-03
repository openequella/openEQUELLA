package com.tle.core.hibernate.equella.service;

import java.util.function.Function;

public interface InitialiserService
{
	<T> T unwrapHibernate(T object);

	<T> T initialise(T object);

	<T> T initialise(T object, InitialiserCallback callback);

	void initialiseClones(Object item);

	<T> Function<T, T> createCloner(ClassLoader classLoader);
}