package com.tle.common.harvester;

@SuppressWarnings("nls")
public class MEXHarvesterSettings extends AbstractTLFHarvesterSettings
{
	public static final String MEX_HARVESTER_TYPE = "MEXHarvesterSettings";
	public static final String MEX_SERVER_URL = "http://mex.thelearningfederation.edu.au";

	public MEXHarvesterSettings()
	{
		super();
	}

	public MEXHarvesterSettings(HarvesterProfile gateway)
	{
		super(gateway);
	}

	@Override
	protected String getType()
	{
		return MEX_HARVESTER_TYPE;
	}

	@Override
	public String getServer()
	{
		return MEX_SERVER_URL;
	}
}
