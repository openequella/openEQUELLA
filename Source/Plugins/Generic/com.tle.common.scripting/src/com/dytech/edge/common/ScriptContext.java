package com.dytech.edge.common;

import java.io.Serializable;
import java.util.Map;

import com.tle.common.util.Logger;

/**
 * @author aholland
 */
public interface ScriptContext extends Serializable
{
	PropBagWrapper getXml();

	Logger getLogger();

	void setLogger(Logger logger);

	/**
	 * Invokes scriptEnter on all the script objects
	 */
	void scriptEnter();

	/**
	 * Invokes scriptExit on all the script objects
	 */
	void scriptExit();

	/**
	 * Don't ever use this, other than Wizard/Workflow adding extra objects
	 * 
	 * @param name
	 * @param object
	 */
	void addScriptObject(String name, Object object);

	/**
	 * add all the User defined javascript
	 * 
	 * @param moduleName
	 * @param script
	 */
	void addUserScriptObject(String moduleName, Object script);

	/**
	 * @return A *read-only* map of script objects
	 */
	Map<String, Object> getScriptObjects();

	/**
	 * @Return A *read-only* map of UserScript objects
	 */
	Map<String, Object> getUserScriptObjects();

}