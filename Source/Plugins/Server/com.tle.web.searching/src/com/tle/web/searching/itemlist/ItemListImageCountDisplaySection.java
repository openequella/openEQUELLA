package com.tle.web.searching.itemlist;

import java.util.List;

import com.google.inject.Inject;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.itemlist.item.StandardItemListEntry;
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
public class ItemListImageCountDisplaySection extends ItemListFileCountDisplaySection
{
	@PlugKey("images.count")
	private static String COUNT_KEY;

	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	public ProcessEntryCallback<Item, StandardItemListEntry> processEntries(final RenderContext context,
		List<StandardItemListEntry> entries, ListSettings<StandardItemListEntry> listSettings)
	{
		final boolean countDisabled = isFileCountDisabled();

		return new ProcessEntryCallback<Item, StandardItemListEntry>()
		{
			@Override
			public void processEntry(StandardItemListEntry entry)
			{
				if( !countDisabled )
				{
					final boolean canViewRestricted = canViewRestricted(entry.getItem());

					// Optimised?
					final List<FileAttachment> fileatts = entry.getAttachments().getList(AttachmentType.FILE);
					long count = fileatts.stream().filter(fa -> {
						if( canViewRestricted || !fa.isRestricted() )
						{
							String mimeType = mimeTypeService.getMimeTypeForFilename(fa.getFilename());
							return mimeType.startsWith("image");
						}
						return false;
					}).count();

					if( count > 1 )
					{
						// disabled link renders as a span, deel wiv it
						HtmlLinkState link = new HtmlLinkState(new IconLabel(Icon.IMAGE, new CountLabel(count), false));
						link.setDisabled(true);
						link.setTitle(new PluralKeyLabel(COUNT_KEY, count));
						entry.setThumbnailCount(new DivRenderer("filecount", new LinkRenderer(link)));
					}
				}
			}
		};
	}
}
