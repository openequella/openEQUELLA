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
