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

/**
 * Used as the return results from ConnectionScriptType, which in-turn is
 * retrieved by UtilsScriptObject.getConnection(String)
 * 
 * @author aholland
 */
public interface ResponseScriptType
{
	/**
	 * @return The response from the external site is an error code, or there
	 *         was a problem connecting to the server.
	 */
	boolean isError();

	/**
	 * @return The response code from the external site
	 */
	int getCode();

	/**
	 * @return The MIME type as returned in the Content-Type header from the
	 *         external site
	 */
	String getContentType();

	/**
	 * @return Get the data from the response in plain text format
	 */
	String getAsText();

	/**
	 * Get the data from the response in XML format This method will throw an
	 * exception if the returned text is not valid XML.
	 * 
	 * @return An XmlScriptType object representing the data in the response.
	 */
	XmlScriptType getAsXml();

	/**
	 * Get the HTML data from the response and tidy it to be valid XML. This
	 * method will throw an exception if the returned text could not be tidied
	 * into valid XML.
	 * 
	 * @return An XmlScriptType object representing the data in the response.
	 */
	XmlScriptType getHtmlAsXml();

	/**
	 * For retrieving binary data such as images
	 * 
	 * @return A BinaryDataScriptType object
	 */
	BinaryDataScriptType getAsBinaryData();
}
