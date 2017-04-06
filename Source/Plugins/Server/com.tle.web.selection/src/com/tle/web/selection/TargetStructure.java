package com.tle.web.selection;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author Aaron
 */
@NonNullByDefault
public class TargetStructure extends TargetFolder
{
	private Map<String, String> attributes = Maps.newHashMap();
	private boolean noTargets;

	public Map<String, String> getAttributes()
	{
		return Collections.unmodifiableMap(attributes);
	}

	public void putAttribute(String key, String value)
	{
		attributes.put(key, value);
	}

	@Nullable
	public String getAttribute(String key)
	{
		return attributes.get(key);
	}

	public boolean isNoTargets()
	{
		return noTargets;
	}

	public void setNoTargets(boolean noTargets)
	{
		this.noTargets = noTargets;
	}
}
