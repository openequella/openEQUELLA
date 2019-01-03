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

package com.tle.web.viewurl;

import java.util.HashMap;
import java.util.Map;

public class ResourceViewerConfig
{
	private boolean openInNewWindow;
	private String width;
	private String height;
	private boolean thickbox;
	private Map<String, Object> attr = new HashMap<String, Object>();

	public boolean isOpenInNewWindow()
	{
		return openInNewWindow;
	}

	public void setOpenInNewWindow(boolean openInNewWindow)
	{
		this.openInNewWindow = openInNewWindow;
	}

	public String getWidth()
	{
		return width;
	}

	public void setWidth(String width)
	{
		this.width = width;
	}

	public String getHeight()
	{
		return height;
	}

	public void setHeight(String height)
	{
		this.height = height;
	}

	public Map<String, Object> getAttr()
	{
		return attr;
	}

	public void setAttr(Map<String, Object> attrs)
	{
		this.attr = attrs;
	}

	public boolean isThickbox()
	{
		return thickbox;
	}

	public void setThickbox(boolean thickbox)
	{
		this.thickbox = thickbox;
	}

}
