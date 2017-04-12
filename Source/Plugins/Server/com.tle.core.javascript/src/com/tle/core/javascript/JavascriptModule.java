package com.tle.core.javascript;

import java.io.Serializable;

/**
 * The JavascriptLibrary is the top level (e.g. JQuery), the JavascriptModule is
 * a subset of the library (e.g. JQuery UI)
 * 
 * @author aholland
 */
public interface JavascriptModule extends Serializable
{
	String getId();

	String getDisplayName();

	/**
	 * Don't assume that you won't get a null back from this. You may very well
	 * get one.
	 * 
	 * @return Usually (ok, always) a sections PreRenderable. Obviously a core
	 *         plugin cannot reference a web one though.
	 */
	Object getPreRenderer();
}
