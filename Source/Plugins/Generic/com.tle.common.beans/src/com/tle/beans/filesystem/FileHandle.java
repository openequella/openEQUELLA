package com.tle.beans.filesystem;

import java.io.Serializable;

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
}
