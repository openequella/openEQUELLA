package com.tle.common.harvester;

@SuppressWarnings("nls")
public class LORAXHarvesterSettings extends AbstractTLFHarvesterSettings
{
	private static final String LORAX_HARVESTER_TYPE = "LORAXHarvesterSettings";
	public static final String LORAX_SERVER_URL = "http://lex.thelearningfederation.edu.au";

	public LORAXHarvesterSettings()
	{
		super();
	}

	public LORAXHarvesterSettings(HarvesterProfile gateway)
	{
		super(gateway);
	}

	@Override
	protected String getType()
	{
		return LORAX_HARVESTER_TYPE;
	}

	@Override
	public String getServer()
	{
		return LORAX_SERVER_URL;
	}
}
