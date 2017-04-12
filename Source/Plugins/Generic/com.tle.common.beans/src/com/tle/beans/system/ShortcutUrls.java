/*
 * Created on May 25, 2005
 */
package com.tle.beans.system;

import java.util.HashMap;
import java.util.Map;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.PropertyHashMap;

/**
 * @author Nicholas Read
 */
public class ShortcutUrls implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;

	@PropertyHashMap(key = "shortcuts")
	private final Map<String, String> shortcuts = new HashMap<String, String>();

	public Map<String, String> getShortcuts()
	{
		return shortcuts;
	}
}
