package com.tle.core.javascript;

import java.io.Serializable;
import java.util.Map;

/**
 * The JavascriptLibrary is the top level (e.g. JQuery), the JavascriptModule is
 * a subset of the library (e.g. JQuery UI) TODO: will need to rename all
 * xxLibrary classes to avoid confusion
 * 
 * @author aholland
 */
public interface JavascriptLibrary extends Serializable
{
	String getId();

	String getDisplayName();

	Map<String, JavascriptModule> getModules();
}
