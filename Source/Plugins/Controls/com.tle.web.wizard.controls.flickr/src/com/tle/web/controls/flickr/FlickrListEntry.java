/**
 *
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
