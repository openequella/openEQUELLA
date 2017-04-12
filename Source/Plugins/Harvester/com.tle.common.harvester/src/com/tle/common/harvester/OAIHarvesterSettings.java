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
