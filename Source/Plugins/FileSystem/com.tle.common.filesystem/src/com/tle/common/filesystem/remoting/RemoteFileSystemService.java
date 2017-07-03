package com.tle.common.filesystem.remoting;

import java.util.List;

import com.tle.common.NameValue;

/**
 * @author Aaron
 *
 */
@SuppressWarnings("nls")
public interface RemoteFileSystemService
{
	String DEFAULT_FILESTORE_ID = "default";

	List<NameValue> listFilestores();

	boolean isAdvancedFilestore();
}
