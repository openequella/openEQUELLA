package com.tle.core.services.config;

import java.util.List;
import java.util.Map;

import com.tle.beans.system.AutoLogin;
import com.tle.common.property.ConfigurationProperties;
import com.tle.core.services.impl.ProxyDetails;

/**
 * @author Nicholas Read
 */
public interface ConfigurationService
{
	boolean isAutoTestMode();

	boolean isDebuggingMode();

	boolean isAutoLoginAvailable(AutoLogin autoLogin, String ipAddress);

	boolean isAutoLoginAvailable(AutoLogin autoLogin);

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
