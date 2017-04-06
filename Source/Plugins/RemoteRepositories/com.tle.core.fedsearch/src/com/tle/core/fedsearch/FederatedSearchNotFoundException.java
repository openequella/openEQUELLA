package com.tle.core.fedsearch;

import com.dytech.edge.exceptions.SearchingException;

/**
 * @author Nicholas Read
 */
public class FederatedSearchNotFoundException extends SearchingException
{
	private static final long serialVersionUID = 1L;

	public FederatedSearchNotFoundException(String gateway)
	{
		super("The federated search gateway '" + gateway + "' could not be found");
	}
}
