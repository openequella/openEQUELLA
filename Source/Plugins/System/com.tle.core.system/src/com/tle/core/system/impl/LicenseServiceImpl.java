package com.tle.core.system.impl;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.valuebean.License;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.event.SchemaListener;
import com.tle.core.services.EventService;
import com.tle.core.system.LicenseService;
import com.tle.core.system.SystemConfigService;
import com.tle.core.system.events.LicenseUpdateEvent;

/**
 * @author Nicholas Read
 */
@Bind(LicenseService.class)
@Singleton
public class LicenseServiceImpl implements LicenseService, SchemaListener, LicenseServiceListener
{
	@Inject
	private SystemConfigService systemConfigService;
	@Inject
	private EventService eventService;

	private boolean licenseLoaded;
	private License license;

	@Override
	public License getLicense()
	{
		return license;
	}

	@Override
	public void setLicense(License license)
	{
		String base64 = license.encrypt();
		systemConfigService.setLicense(base64);
		eventService.publishApplicationEvent(new LicenseServiceEvent(license));
		newLicense(license);
	}

	private void loadLicense()
	{
		String licenseBase64 = systemConfigService.getLicense();
		if( licenseBase64 != null )
		{
			License newLicense = new License();
			newLicense.decrypt(licenseBase64);
			newLicense(newLicense);
		}
		licenseLoaded = true;
	}

	@Override
	public synchronized void systemSchemaUp()
	{
		loadLicense();
	}

	@Override
	public void schemasAvailable(Collection<Long> schemas)
	{
		// no care
	}

	@Override
	public void schemasUnavailable(Collection<Long> schemas)
	{
		// no care
	}

	@Override
	public void newLicense(License license)
	{
		this.license = license;
		eventService.publishApplicationEvent(new LicenseUpdateEvent());
	}

	@Override
	public boolean isLicenseLoaded()
	{
		return licenseLoaded;
	}

	@Override
	public boolean isFeatureEnabled(String feature)
	{
		if( !isLicenseLoaded() )
		{
			return false;
		}
		return isDevBuild() || license.getFeatures().contains(feature);
	}

	@Override
	public boolean isDevBuild()
	{
		return license.getVersion().startsWith(License.DEVELOPMENT_BUILD);
	}
}
