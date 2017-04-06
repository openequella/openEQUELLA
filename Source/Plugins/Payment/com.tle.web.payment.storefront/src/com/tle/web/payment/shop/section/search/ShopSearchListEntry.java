package com.tle.web.payment.shop.section.search;

import java.util.List;

import com.tle.core.guice.Bind;
import com.tle.web.itemlist.MetadataEntry;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.HighlightableBundleLabel;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * @author Aaron
 */
@Bind
public class ShopSearchListEntry extends AbstractItemListEntry
{
	private HtmlLinkState title;
	private Label description;
	private HtmlBooleanState checkbox;
	private List<MetadataEntry> metadata;

	@Override
	public HtmlLinkState getTitle()
	{
		return title;
	}

	@Override
	public Label getDescription()
	{
		return new HighlightableBundleLabel(null, description, bundleCache, listSettings.getHilightedWords(), true);
	}

	public void setTitle(HtmlLinkState title)
	{
		this.title = title;
	}

	public void setDescription(Label description)
	{
		this.description = description;
	}

	@Override
	public HtmlBooleanState getCheckbox()
	{
		return checkbox;
	}

	public void setCheckBox(HtmlBooleanState checkbox)
	{
		this.checkbox = checkbox;
	}

	@Override
	public List<MetadataEntry> getMetadata()
	{
		return metadata;
	}

	public void setMetadata(List<MetadataEntry> metadata)
	{
		this.metadata = metadata;
	}

	@Override
	protected void setupMetadata(RenderContext context)
	{
		// Stop super from calling
	}

	@Override
	public void init(RenderContext context, ListSettings<?> settings)
	{
		this.listSettings = settings;
		title.setLabel(new HighlightableBundleLabel(null, title.getLabel(), bundleCache, listSettings
			.getHilightedWords(), false));
	}
}
