package com.dytech.edge.importexport;

import java.util.HashMap;

import org.apache.axis.AxisProperties;
import org.apache.axis.components.net.DefaultHTTPSTransportClientProperties;
import org.apache.axis.components.net.DefaultHTTPTransportClientProperties;
import org.apache.axis.components.net.TransportClientProperties;

/**
 * Basically an exact copy of TransportClientPropertiesFactory but it doesnt
 * cache the proxy values
 * 
 * @author will
 */
public class TleTransportClientPropertiesFactory
{
	private static HashMap defaults = new HashMap();

	static
	{
		defaults.put("http", DefaultHTTPTransportClientProperties.class);
		defaults.put("https", DefaultHTTPSTransportClientProperties.class);
	}

	public static TransportClientProperties create(String protocol)
	{
		TransportClientProperties tcp;

		tcp = (TransportClientProperties) AxisProperties.newInstance(TransportClientProperties.class,
			(Class) defaults.get(protocol));

		return tcp;
	}
}
