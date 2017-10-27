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
