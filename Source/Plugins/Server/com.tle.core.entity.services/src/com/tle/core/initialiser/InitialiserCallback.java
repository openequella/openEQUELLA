package com.tle.core.initialiser;

public interface InitialiserCallback
{
	void set(Object obj, Property property, Object value);

	void entitySimplified(Object old, Object newObj);
}
