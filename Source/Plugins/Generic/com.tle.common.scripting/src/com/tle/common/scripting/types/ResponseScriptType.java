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
