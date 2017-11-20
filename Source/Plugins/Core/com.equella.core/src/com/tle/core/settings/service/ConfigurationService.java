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

package com.tle.core.settings.service;

import java.util.List;
import java.util.Map;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.core.services.impl.ProxyDetails;

/**
 * @author Nicholas Read
 */
public interface ConfigurationService
{
	boolean isAutoTestMode();

	boolean isDebuggingMode();

	// GETTERS ///////////////////////////////////////////////////////////////

	/**
	 * The result of this will be cached for future invocations.
	 */
	<T extends ConfigurationProperties> T getProperties(T empty);

	/**
	 * The result of this will be cached for future invocations.
	 */
	String getProperty(String property);

	/**
	 * The result of this will be cached for future invocations.
	 */
	<T> List<T> getPropertyList(String property);

	// SETTERS ///////////////////////////////////////////////////////////////

	void setProperties(ConfigurationProperties properties);

	void setProperty(String property, String value);

	void deleteProperty(String property);

	// For institution import/export/deletion only ///////////////////////////

	Map<String, String> getAllProperties();

	void importInstitutionProperties(Map<String, String> map);

	void deleteAllInstitutionProperties();

	// Other stuff //////////////////////////////////////////////////////////

	ProxyDetails getProxyDetails();
}
