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

@SuppressWarnings("nls")
public class OAIHarvesterSettings extends HarvesterProfileSettings
{
	private static final String HARVESTER_TYPE = "OAIHarvesterSettings";

	private String server;

	private String aSet;

	private String format;

	public String getaSet()
	{
		return aSet;
	}

	public void setaSet(String aSet)
	{
		this.aSet = aSet;
	}

	public String getServer()
	{
		return server;
	}

	public void setServer(String server)
	{
		this.server = server;
	}

	public OAIHarvesterSettings()
	{
		super();
	}

	public OAIHarvesterSettings(HarvesterProfile gateway)
	{
		super(gateway);
	}

	@Override
	protected void _load()
	{
		server = get("server", server);
		format = get("format", format);
		if( format == null || format.isEmpty() )
		{
			format = "oai_dc";
		}
		aSet = get("aSet", aSet);
	}

	@Override
	protected void _save()
	{
		if( format == null || format.isEmpty() )
		{
			format = "oai_dc";
		}
		put("server", server);
		put("format", format);
		put("aSet", aSet);
	}

	@Override
	protected String getType()
	{
		return HARVESTER_TYPE;
	}

	public void setFormat(String text)
	{
		format = text;

	}

	public String getFormat()
	{
		return format;
	}

}
