package com.tle.web.discoverability.scripting.objects;

import com.tle.common.scripting.ScriptObject;

/**
 * Referenced by the 'meta' variable in scripting
 * 
 * @author wbowling
 */
public interface MetaScriptObject extends ScriptObject
{
	String DEFAULT_VARIABLE = "meta"; //$NON-NLS-1$

	/**
	 * Add a new meta tag to the head element of the item summary page. So
	 * <code>meta.add("citation_title", "Etiam aliquet massa et lorem")</code>
	 * will add the following meta tag:
	 * 
	 * <pre>
	 * {@code
	 * <meta name="citation_title" content="Etiam aliquet massa et lorem">
	 * }
	 * </pre>
	 * 
	 * @param name The name attribute of the meta tag
	 * @param content The content attribute of the meta tag
	 */
	void add(String name, String content);

}
