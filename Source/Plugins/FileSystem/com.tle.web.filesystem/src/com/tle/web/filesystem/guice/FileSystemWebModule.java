package com.tle.web.filesystem.guice;

import javax.inject.Named;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.tle.core.services.FileSystemService;

/**
 * @author Aaron
 *
 */
public class FileSystemWebModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		// Nah
	}

	@Provides
	@Named("remoteFileSystemService")
	Object provideFileSystemService(FileSystemService remote)
	{
		return remote;
	}
}
