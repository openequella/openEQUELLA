package com.tle.web.searching.itemlist;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
import com.tle.web.searching.VideoPreviewRenderer;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.result.util.CountLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@SuppressWarnings("nls")
@Bind
public class ItemListVideoCountDisplaySection extends ItemListFileCountDisplaySection
{
	@Inject
	private PluginTracker<VideoPreviewRenderer> previewRenderers;
	@PlugKey("videos.count")
	private static String COUNT_KEY;

	@Override
	public ProcessEntryCallback<Item, StandardItemListEntry> processEntries(final RenderContext context,
		List<StandardItemListEntry> entries, ListSettings<StandardItemListEntry> listSettings)
	{
		final boolean countDisabled = isFileCountDisabled();
		final List<VideoPreviewRenderer> renderers = previewRenderers.getBeanList();

		return new ProcessEntryCallback<Item, StandardItemListEntry>()
		{
			@Override
			public void processEntry(StandardItemListEntry entry)
			{
				if( !countDisabled )
				{
					final boolean canViewRestricted = canViewRestricted(entry.getItem());

					// Optimised?
					final long count = entry.getViewableResources().stream().filter(vr -> {
						final IAttachment attachment = vr.getAttachment();
						if( (!canViewRestricted && (attachment != null && attachment.isRestricted())) )
						{
							return false;
						}
						final String mimeType = vr.getMimeType();
						for( VideoPreviewRenderer renderer : renderers )
						{
							if( renderer.supports(mimeType) )
							{
								return true;
							}
						}
						return false;
					}).count();

					if( count > 1 )
					{
						HtmlLinkState link = new HtmlLinkState(new IconLabel(Icon.VIDEO, new CountLabel(count), false));
						link.setDisabled(true);
						link.setTitle(new PluralKeyLabel(COUNT_KEY, count));
						entry.setThumbnailCount(new DivRenderer("filecount", new LinkRenderer(link)));
					}
				}
			}
		};
	}
}
