package com.tle.core.services;

import com.tle.core.initialiser.InitialiserCallback;

public interface InitialiserService
{
	<T> T unwrapHibernate(T object);

	<T> T initialise(T object);

	<T> T initialise(T object, InitialiserCallback callback);

	void initialiseClones(Object item);
}