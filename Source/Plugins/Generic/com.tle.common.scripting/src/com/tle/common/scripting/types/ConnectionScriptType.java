/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
