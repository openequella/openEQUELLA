package com.tle.mycontent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jmaginnis
 */
public class MyContentFilter
{
	private final String name;
	private final String icon;
	private final List<String> contentTypes;
	private final List<String> mimeTypes;

	public MyContentFilter(String name, String icon, String... types)
	{
		this(name, icon, new ArrayList<String>(), Arrays.asList(types));
	}

	public MyContentFilter(String name, String icon, List<String> contentTypes, List<String> types)
	{
		this.name = name;
		this.icon = icon;
		this.contentTypes = contentTypes;
		this.mimeTypes = types;
	}

	public String getName()
	{
		return name;
	}

	public String getIcon()
	{
		return icon;
	}

	public List<String> getContentTypes()
	{
		return contentTypes;
	}

	public List<String> getMimeTypes()
	{
		return mimeTypes;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
