/**
 * 
 */
package com.tle.web.sections.registry.handler.util;

import java.lang.reflect.Type;

public interface PropertyAccessor
{
	Object read(Object obj) throws Exception;

	void write(Object obj, Object value) throws Exception;

	Type getType();

	String getName();
}