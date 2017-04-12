package com.tle.core.filesystem;

import java.io.File;

public interface FileCallback
{
	/**
	 * @param file1 The file being processed
	 * @param file2 Usually null. Only set in a dual file operation e.g. copy
	 */
	void fileProcessed(File file1, File file2);
}
