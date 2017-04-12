package com.tle.common.javascript;

import java.util.List;

import com.tle.common.NameValue;

/**
 * @author aholland
 */
public interface RemoteJavascriptService
{
	List<NameValue> getAllJavascriptLibraryNames();

	List<NameValue> getAllJavascriptModuleNames(String libraryId);
}
