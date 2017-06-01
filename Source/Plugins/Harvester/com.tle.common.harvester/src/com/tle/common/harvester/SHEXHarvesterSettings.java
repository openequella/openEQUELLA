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

package com.tle.common.harvester;

/**
 * @author larry
 *
 */
@SuppressWarnings("nls")
public class SHEXHarvesterSettings extends AbstractTLFHarvesterSettings
{
	public static final String SHEX_HARVESTER_TYPE = "SHEXHarvesterSettings";
	public static final String SHEX_SERVER_URL = "http://sharing.thelearningfederation.edu.au";

	public SHEXHarvesterSettings()
	{
		super();
	}

	public SHEXHarvesterSettings(HarvesterProfile gateway)
	{
		super(gateway);
	}

	@Override
	protected String getType()
	{
		return SHEX_HARVESTER_TYPE;
	}

	@Override
	public String getServer()
	{
		return SHEX_SERVER_URL;
	}
}
