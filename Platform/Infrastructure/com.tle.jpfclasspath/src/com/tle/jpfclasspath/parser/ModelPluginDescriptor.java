package com.tle.jpfclasspath.parser;

final public class ModelPluginDescriptor extends ModelPluginManifest
{
	private String className;

	ModelPluginDescriptor()
	{
		// no-op
	}

	String getClassName()
	{
		return className;
	}

	void setClassName(final String value)
	{
		className = value;
	}
}