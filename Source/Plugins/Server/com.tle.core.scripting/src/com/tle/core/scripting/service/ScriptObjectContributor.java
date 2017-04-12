package com.tle.core.scripting.service;

import java.util.Map;

import com.tle.common.scripting.service.ScriptContextCreationParams;

/**
 * Implemented by plugins (including the standard one) to get a series of
 * objects to inject into scripts
 * 
 * @author aholland
 */
public interface ScriptObjectContributor
{
	void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params);
}
