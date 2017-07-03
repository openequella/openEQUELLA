package com.tle.core.hibernate.equella.service;

public interface InitialiserCallback
{
	void set(Object obj, Property property, Object value);

	void entitySimplified(Object old, Object newObj);
}
