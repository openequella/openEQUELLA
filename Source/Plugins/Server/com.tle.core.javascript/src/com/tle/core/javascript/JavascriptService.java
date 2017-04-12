package com.tle.core.javascript;

import java.util.List;

import com.tle.common.javascript.RemoteJavascriptService;

/**
 * @author aholland
 */
public interface JavascriptService extends RemoteJavascriptService
{
	List<JavascriptLibrary> getAllJavascriptLibraries();

	JavascriptLibrary getJavascriptLibrary(String libraryId);

	JavascriptModule getJavascriptModule(String libraryId, String moduleId);
}
