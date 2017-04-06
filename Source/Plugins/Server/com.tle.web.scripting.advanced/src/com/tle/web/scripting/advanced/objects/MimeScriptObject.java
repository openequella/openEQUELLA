package com.tle.web.scripting.advanced.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.web.scripting.advanced.types.MimeTypeScriptType;

/**
 * Referenced by the 'mime' variable in script.
 * 
 * @author aholland
 */
public interface MimeScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "mime"; //$NON-NLS-1$

	/**
	 * Get at MimeTypeScriptType for the given filename (will read the extension
	 * on the filename to determine the MIME type)
	 * 
	 * @param filename The name of the file
	 * @return A MimeTypeScriptType object, or null if the mime type could not
	 *         be determined.
	 */
	MimeTypeScriptType getMimeTypeForFilename(String filename);
}
