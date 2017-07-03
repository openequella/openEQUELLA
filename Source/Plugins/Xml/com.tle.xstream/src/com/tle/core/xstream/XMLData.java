/*
 * Created on Jun 8, 2005
 */
package com.tle.core.xstream;

import java.io.Serializable;

/**
 * @author jmaginnis
 */
public interface XMLData extends Serializable
{
	XMLDataMappings getMappings();
}
