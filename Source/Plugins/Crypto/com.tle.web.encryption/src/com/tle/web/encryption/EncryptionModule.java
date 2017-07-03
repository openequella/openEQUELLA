package com.tle.web.encryption;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.tle.core.encryption.EncryptionService;

public class EncryptionModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		// just provider
	}

	@Provides
	@Named("remoteEncryptionService")
	Object provideEncryptionService(EncryptionService remote)
	{
		return remote;
	}
}
