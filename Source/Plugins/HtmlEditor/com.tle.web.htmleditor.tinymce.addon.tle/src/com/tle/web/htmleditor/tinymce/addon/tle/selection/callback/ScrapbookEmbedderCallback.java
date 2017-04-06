package com.tle.web.htmleditor.tinymce.addon.tle.selection.callback;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.util.UnmodifiableIterable;
import com.tle.core.guice.Bind;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

/**
 * @author aholland
 */
@Bind
@Singleton
public class ScrapbookEmbedderCallback extends AbstractSelectionsMadeCallback
{
	private static final long serialVersionUID = 1L;

	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private MyPagesService myPagesService;

	@Override
	protected ViewableResource createViewableResourceFromSelection(SectionInfo info, SelectedResource res,
		String sessionId, String pageId)
	{
		final SelectedResourceKey key = res.getKey();
		final ItemId itemId = new ItemId(key.getUuid(), key.getVersion());
		final ViewableItem viewableItem = viewableItemFactory.createNewViewableItem(itemId);
		final UnmodifiableIterable<Attachment> attachments = viewableItem.getItem().getAttachmentsUnmodifiable();
		final Attachment attachment = attachments.get(0);
		final ViewableResource vres = attachmentResourceService.getViewableResource(info, viewableItem, attachment);
		return myPagesService.cloneMyContent(info, vres, sessionId, pageId);
	}
}
