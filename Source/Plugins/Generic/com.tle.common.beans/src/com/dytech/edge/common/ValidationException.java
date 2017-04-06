/*
 * ValidationException.java Created on 26 August 2002, 12:50
 */

package com.dytech.edge.common;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.exceptions.RuntimeApplicationException;

/**
 * @author jmaginnis
 */
public class ValidationException extends RuntimeApplicationException
{
	private static final long serialVersionUID = 1L;
	private PropBagEx xml;

	public ValidationException(String message)
	{
		super(message);
	}

	public ValidationException(PropBagEx valBag)
	{
		xml = valBag;
	}

	public ValidationException(String node, String value)
	{
		super(value);

		xml = new PropBagEx();
		xml.setNode(node, value);
	}

	public PropBagEx getValPropBag()
	{
		return xml;
	}
}
