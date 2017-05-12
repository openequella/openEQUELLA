package com.tle.jpfclasspath.parser;


final public class ModelPrerequisite
{
	private String id;
	private String pluginId;
	private Version pluginVersion;
	private MatchingRule matchingRule = MatchingRule.COMPATIBLE;
	private ModelDocumentation documentation;
	private boolean isExported;
	private boolean isOptional;
	private boolean isReverseLookup;

	public ModelPrerequisite()
	{
		// no-op
	}

	public ModelDocumentation getDocumentation()
	{
		return documentation;
	}

	void setDocumentation(final ModelDocumentation value)
	{
		documentation = value;
	}

	public String getId()
	{
		return id;
	}

	void setId(final String value)
	{
		id = value;
	}

	public boolean isExported()
	{
		return isExported;
	}

	void setExported(final String value)
	{
		isExported = "true".equals(value); //$NON-NLS-1$
	}

	public boolean isOptional()
	{
		return isOptional;
	}

	void setOptional(final String value)
	{
		isOptional = "true".equals(value); //$NON-NLS-1$
	}

	public boolean isReverseLookup()
	{
		return isReverseLookup;
	}

	void setReverseLookup(final String value)
	{
		isReverseLookup = "true".equals(value); //$NON-NLS-1$
	}

	MatchingRule getMatchingRule()
	{
		return matchingRule;
	}

	void setMatchingRule(final MatchingRule value)
	{
		matchingRule = value;
	}

	public String getPluginId()
	{
		return pluginId;
	}

	void setPluginId(final String value)
	{
		pluginId = value;
	}

	public Version getPluginVersion()
	{
		return pluginVersion;
	}

	void setPluginVersion(final String value)
	{
		pluginVersion = Version.parse(value);
	}
}