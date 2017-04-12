package com.tle.admin.helper;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dytech.gui.JImage;

/**
 * Created Jan 14, 2004
 * 
 * @author Nicholas Read
 */
public class IconContainer
{
	private Map<JImage, String> map;

	public IconContainer()
	{
		map = new LinkedHashMap<JImage, String>();
	}

	public void addIcon(String path, JImage image)
	{
		map.put(image, path);
	}

	public String getPath(JImage image)
	{
		return map.get(image);
	}

	public Iterator<JImage> iterateImages()
	{
		return map.keySet().iterator();
	}

	public int getIconCount()
	{
		return map.size();
	}
}
