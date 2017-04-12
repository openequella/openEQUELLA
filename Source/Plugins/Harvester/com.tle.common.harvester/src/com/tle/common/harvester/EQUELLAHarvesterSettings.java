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
