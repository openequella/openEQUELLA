package com.tle.common.scripting.types;

import java.io.Serializable;
import java.util.Map;

// FIXME: not implemented
/**
 * @author Aaron
 */
public interface HtmlTagScriptType extends Serializable
{
	String getName();

	HtmlTagScriptType setName();

	Map<String, String> getAttributes();

}
