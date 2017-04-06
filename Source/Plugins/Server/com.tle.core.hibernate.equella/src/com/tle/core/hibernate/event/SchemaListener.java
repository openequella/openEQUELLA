package com.tle.core.hibernate.event;

import java.util.Collection;

import com.tle.core.events.listeners.ApplicationListener;

public interface SchemaListener extends ApplicationListener
{
	void systemSchemaUp();

	void schemasAvailable(Collection<Long> schemas);

	void schemasUnavailable(Collection<Long> schemas);

}
