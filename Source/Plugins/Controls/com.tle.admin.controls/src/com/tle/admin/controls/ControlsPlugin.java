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

package com.tle.admin.controls;

import org.java.plugin.Plugin;

import com.tle.admin.Driver;
import com.tle.admin.Driver.ControlsRepositoryCreator;
import com.tle.admin.controls.repository.ControlRepository;

public class ControlsPlugin extends Plugin implements ControlsRepositoryCreator
{
	private Driver driver;

	@Override
	protected void doStart() throws Exception
	{
		this.driver = Driver.instance();
		driver.registerControlRepositoryCreator(this);
	}

	@Override
	protected void doStop() throws Exception
	{
		// nothing
	}

	@Override
	public ControlRepository getControlRepository()
	{
		return new ControlRepositoryImpl(driver.getPluginService());
	}

}
