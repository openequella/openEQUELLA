/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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