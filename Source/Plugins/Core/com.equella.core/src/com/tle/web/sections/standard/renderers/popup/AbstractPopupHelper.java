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

package com.tle.web.sections.standard.renderers.popup;

import com.tle.common.Check;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class AbstractPopupHelper
{
	protected String target;
	protected String width;
	protected String height;

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		this.target = target;
	}

	public String getWidth()
	{
		return width;
	}

	public void setWidth(String width)
	{
		if( !Check.isEmpty(width) )
		{
			this.width = removePixels(width);
		}
	}

	private String removePixels(String size)
	{
		if( size.endsWith("px") ) //$NON-NLS-1$
		{
			size = size.substring(0, size.length() - 2);
		}
		return size;
	}

	public String getHeight()
	{
		return height;
	}

	public void setHeight(String height)
	{
		if( !Check.isEmpty(height) )
		{
			this.height = removePixels(height);
		}
	}

	public boolean hasServerBookmark(SectionInfo info, HtmlComponentState state)
	{
		JSHandler clickHandler = state.getHandler(JSHandler.EVENT_CLICK);
		if( clickHandler == null )
		{
			return false;
		}
		JSBookmarkModifier modifier = clickHandler.getModifier();
		if( modifier == null )
		{
			return false;
		}
		return !modifier.hasClientModifications();
	}

	public boolean hasClientBookmark(SectionInfo info, HtmlComponentState state)
	{
		JSHandler clickHandler = state.getHandler(JSHandler.EVENT_CLICK);
		if( clickHandler == null )
		{
			return false;
		}
		JSBookmarkModifier modifier = clickHandler.getModifier();
		if( modifier == null )
		{
			return false;
		}
		return modifier.hasClientModifications();
	}

	public String getHrefForClickEvent(SectionInfo info, JSHandler clickHandler)
	{
		return new BookmarkAndModify(info, clickHandler.getModifier()).getHref();
	}

}
