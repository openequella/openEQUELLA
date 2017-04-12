package com.tle.core.metadata.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.FileHandleScriptType;
import com.tle.core.metadata.scripting.types.MetadataScriptType;

/**
 * Referenced by the 'metadata' variable in a script
 */
@SuppressWarnings("nls")
public interface MetadataScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "metadata";

	/**
	 * Returns a {@link MetadataScriptType} object for the file provided. If the
	 * file does not exist or cannot be read an empty object will be returned.
	 * 
	 * @param file
	 * @return the metadata for the file
	 */
	MetadataScriptType getMetadata(FileHandleScriptType f);

	/**
	 * Returns a {@link MetadataScriptType} object for the attachment provided.
	 * If the attachment type is unsupported an empty object will be returned
	 * 
	 * @param attachment
	 * @return the metadata for the attachment
	 */
	MetadataScriptType getMetadata(AttachmentScriptType a);
}
