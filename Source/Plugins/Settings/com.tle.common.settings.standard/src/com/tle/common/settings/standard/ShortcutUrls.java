/*
 * Created on May 25, 2005
 */
package com.tle.common.settings.standard;

import java.util.HashMap;
import java.util.Map;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.PropertyHashMap;

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
