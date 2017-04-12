package com.tle.common.scripting.types;

import java.io.Serializable;

/**
 * Used for connecting to external sites from the server.
 * 
 * @author aholland
 */
public interface ConnectionScriptType extends Serializable
{
	/**
	 * Add some data to be sent in the next call to getResponse(boolean) This
	 * means that if you call getResponse(boolean) with the doGET parameter set
	 * to true, the query string of the original URL will be modified to append
	 * the additional data.
	 * 
	 * @param key The key of the form data
	 * @param value The value of the data
	 */
	void addFormData(String key, String value);

	/**
	 * Retrieve data from the connection via GET or POST depending on the value
	 * of parameter doGET
	 * 
	 * @param doGET Perform a GET request (the target URL will be modified with
	 *            the values in the form data that have been added using
	 *            addFormData(String, String) ). If false, a POST will be
	 *            performed.
	 * @return The response from the external site
	 */
	ResponseScriptType getResponse(boolean doGET);
}
