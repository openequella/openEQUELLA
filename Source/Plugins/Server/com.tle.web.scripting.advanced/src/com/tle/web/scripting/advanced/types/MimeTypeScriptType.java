package com.tle.web.scripting.advanced.types;

import java.io.Serializable;
import java.util.List;

/**
 * A MIME type object for scripts
 */
public interface MimeTypeScriptType extends Serializable
{
	/**
	 * @return The MIME type E.g. text/plain
	 */
	String getType();

	/**
	 * @return A friendly name for the MIME type
	 */
	String getDescription();

	/**
	 * @return The list of known file extensions for this MIME type
	 */
	List<String> getFileExtensions();
}
