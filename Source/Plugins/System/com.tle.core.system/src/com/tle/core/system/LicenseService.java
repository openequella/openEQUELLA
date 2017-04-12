package com.tle.core.system;

import com.dytech.edge.common.valuebean.License;

public interface LicenseService
{
	boolean isLicenseLoaded();

	License getLicense();

	void setLicense(License license);

	boolean isFeatureEnabled(String feature);

	boolean isDevBuild();
}
