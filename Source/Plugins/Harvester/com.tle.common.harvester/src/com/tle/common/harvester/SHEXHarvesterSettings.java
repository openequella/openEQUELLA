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
