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
public class EQUELLAHarvesterSettings extends HarvesterProfileSettings
{
	private static final String HARVESTER_TYPE = "EQUELLAHarvesterSettings";

	private String server;

	private String user;
	private String pass;
	private String collection;
	private String collectionName;

	private boolean liveOnly;

	public String getServer()
	{
		return server;
	}

	public void setServer(String server)
	{
		this.server = server;
	}

	public EQUELLAHarvesterSettings()
	{
		super();
	}

	public EQUELLAHarvesterSettings(HarvesterProfile gateway)
	{
		super(gateway);
	}

	@Override
	protected void _load()
	{
		server = get("server", server);
		user = get("user", user);
		pass = get("pass", pass);
		collection = get("collection", collection);
		collectionName = get("collectionName", collectionName);
		liveOnly = get("liveOnly", true);
	}

	@Override
	protected void _save()
	{
		put("server", server);
		put("user", user);
		put("pass", pass);
		put("collection", collection);
		put("collectionName", collectionName);
		put("liveOnly", liveOnly);
	}

	@Override
	protected String getType()
	{
		return HARVESTER_TYPE;
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

	public String getCollection()
	{
		return collection;
	}

	public void setCollection(String collection)
	{
		this.collection = collection;
	}

	public void setLiveOnly(boolean liveOnly)
	{
		this.liveOnly = liveOnly;
	}

	public boolean isLiveOnly()
	{
		return liveOnly;
	}

	public void setCollectionName(String collectionName)
	{
		this.collectionName = collectionName;
	}

	public String getCollectionName()
	{
		return collectionName;
	}
}
