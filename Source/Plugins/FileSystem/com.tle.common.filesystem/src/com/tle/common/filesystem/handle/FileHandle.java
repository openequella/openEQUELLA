package com.tle.common.filesystem.handle;

import java.io.Serializable;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

@NonNullByDefault
public interface FileHandle extends Serializable
{
	/**
	 * Not an absolute path in the truest sense, but if you consider the
	 * filestore folder the root of everything, then the path returned by this
	 * method is absolute.
	 * 
	 * @return The path of this handle relative to the filestore folder.
	 */
	String getAbsolutePath();

	/**
	 * Gets only the path component specified by this handle
	 * 
	 * @return
	 */
	String getMyPathComponent();

	/**
	 * Gets the ID of the filestore (or null for the default as defined by filestore.root in mandatory-config.properties).
	 * Generally only applicable to ItemFile
	 * @return The short ID of the filestore.
	 */
	@Nullable
	String getFilestoreId();
}
