/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
