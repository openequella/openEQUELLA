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

package com.tle.web.controls.flickr;

import java.util.List;

import com.flickr4java.flickr.photos.Photo;
import com.tle.common.Check;
import com.tle.web.itemlist.ListEntry;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.itemlist.item.AbstractListEntry;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author larry
 */
public class FlickrListEntry extends AbstractListEntry
{
	private final Photo photo;

	public FlickrListEntry(Photo photo)
	{
		this.photo = photo;
	}

	@Override
	public void init(RenderContext context, ListSettings<? extends ListEntry> settings)
	{
		// Don't want to
	}

	@Override
	public HtmlLinkState getTitle()
	{
		HtmlLinkState state = new HtmlLinkState();
		state.setLabel(new TextLabel(photo.getTitle()));
		return state;
	}

	/**
	 * Not all photo's have descriptions, but the label render doesn't like
	 * null.
	 * 
	 * @see com.tle.web.sections.equella.list.ListEntry#getDescription()
	 */
	@Override
	public Label getDescription()
	{
		return new TextLabel(Check.nullToEmpty(photo.getDescription()));
	}

	@Override
	public HtmlBooleanState getCheckbox()
	{
		return new HtmlBooleanState();
	}

	@Override
	public List<MetadataEntry> getMetadata()
	{
		return null;
	}

	public Photo getPhoto()
	{
		return photo;
	}
}
