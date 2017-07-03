/*
 * Created on Jun 22, 2005
 */
package com.tle.core.xstream;

import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * 
 */
public interface XMLDataResolver
{
	Class resolveClass(HierarchicalStreamReader reader);

	void writeClass(HierarchicalStreamWriter writer, Object object);
}
