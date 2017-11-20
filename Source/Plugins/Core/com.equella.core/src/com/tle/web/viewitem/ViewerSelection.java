/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.viewitem;

import com.tle.web.viewurl.ViewItemViewer;

public class ViewerSelection
{
	private ViewItemViewer viewer;
	private String viewerId;

	public ViewItemViewer getViewer()
	{
		return viewer;
	}

	public void setViewer(ViewItemViewer viewer)
	{
		this.viewer = viewer;
	}

	public String getViewerId()
	{
		return viewerId;
	}

	public void setViewerId(String viewerId)
	{
		this.viewerId = viewerId;
	}

	public boolean isViewerSet()
	{
		return viewerId != null || viewer != null;
	}
}
