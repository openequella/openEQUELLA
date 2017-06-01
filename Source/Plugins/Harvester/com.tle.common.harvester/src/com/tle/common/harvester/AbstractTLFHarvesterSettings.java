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
 * With the addition of the SHEX and MEX harvesters which differ from the LORAX
 * harvester only in minor details, this class represents a common TLF harvester
 * to house the common code. The source for this code is the
 * LORAXHarvesterSettings class. That class meanwhile is altered into a subclass
 * of this class.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
public abstract class AbstractTLFHarvesterSettings extends HarvesterProfileSettings
{

	private String user;
	private String pass;
	private String server = getServer();

	private boolean liveOnly;
	private boolean harvestLearningObjects;
	private boolean harvestResources;

	public AbstractTLFHarvesterSettings()
	{
		super();
	}

	public AbstractTLFHarvesterSettings(HarvesterProfile gateway)
	{
		super(gateway);
	}

	public abstract String getServer();

	@Override
	protected void _load()
	{
		server = get("server", server);
		user = get("user", user);
		pass = get("pass", pass);
		harvestLearningObjects = get("harvestLearningObjects", true);
		harvestResources = get("harvestResources", false);
		liveOnly = get("liveOnly", true);
	}

	@Override
	protected void _save()
	{
		put("server", server);
		put("user", user);
		put("pass", pass);
		put("harvestLearningObjects", harvestLearningObjects);
		put("harvestResources", harvestResources);
		put("liveOnly", liveOnly);
	}
	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getPass()
	{
		return pass;
	}

	public void setPass(String pass)
	{
		this.pass = pass;
	}

	public Boolean getLiveOnly()
	{
		return liveOnly;
	}

	public void setLiveOnly(Boolean liveOnly)
	{
		this.liveOnly = liveOnly;
	}

	public void setHarvestLearningObjects(Boolean harvestLearningObjects)
	{
		this.harvestLearningObjects = harvestLearningObjects;
	}

	public boolean getHarvestLearningObjects()
	{
		return harvestLearningObjects;
	}

	public void setHarvestResources(Boolean harvestResources)
	{
		this.harvestResources = harvestResources;
	}

	public boolean getHarvestResources()
	{
		return harvestResources;
	}
}
