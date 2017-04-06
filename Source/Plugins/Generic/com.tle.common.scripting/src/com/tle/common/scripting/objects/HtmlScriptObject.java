package com.tle.common.scripting.objects;

import com.tle.common.scripting.ScriptObject;
import com.tle.common.scripting.types.HtmlTagScriptType;

// FIXME: not implemented
/**
 * Referenced by the 'html' variable in script
 * 
 * @author Aaron
 */
public interface HtmlScriptObject extends ScriptObject
{
	String[] getLinks(String value);

	HtmlScriptObject updateLinks(String value, String[] links);

	HtmlTagScriptType[] getTags(String value);

	HtmlScriptObject updateLinks(String value, HtmlTagScriptType[] links);
}
