package com.tle.web.api.payment.backend.links.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Maps;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.core.guice.Bind;
import com.tle.core.payment.beans.store.StoreCatalogueAttachmentBean;
import com.tle.core.payment.beans.store.StoreCatalogueItemBean;
import com.tle.core.services.UrlService;
import com.tle.core.services.item.ItemService;
import com.tle.web.api.payment.backend.links.StoreBeanLinkService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.viewable.ViewItemLinkFactory;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@Bind(StoreBeanLinkService.class)
@Singleton
@SuppressWarnings("nls")
public class StoreBeanLinkServiceImpl implements StoreBeanLinkService
{
	public static final String UNSUPPORTED = "unsupported";

	@Inject
	private ViewItemLinkFactory linkFactory;
	@Inject
	private ItemService itemService;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewableItemFactory viewableItemFactory;
	@Inject
	private SectionsController sectionsController;
	@Inject
	private UrlService urlService;

	/**
	 * @param storeItemBean
	 * @param catalogueUuid
	 * @param lite: use lite when getting results for searches (no preview
	 *            links, only custom thumbs)
	 */
	@Override
	public void addLinks(StoreCatalogueItemBean storeItemBean, String catalogueUuid, boolean lite)
	{
		// Self links here if required
		if( storeItemBean.getAttachments() == null )
		{
			return;
		}
		for( StoreCatalogueAttachmentBean attachment : storeItemBean.getAttachments() )
		{
			final Map<String, String> links = Maps.newHashMap();
			attachment.set("links", links);

			final ItemId id = new ItemId(storeItemBean.getUuid(), itemService.getLiveItemVersion(storeItemBean
				.getUuid()));
			final String uuid = attachment.getUuid();

			ViewableItem vitem = viewableItemFactory.createNewViewableItem(id);
			Attachment realAttachment = itemService.getAttachmentForUuid(id, uuid);
			ViewableResource vr = attachmentResourceService.getViewableResource(getInfo(), vitem, realAttachment);

			if( vr.isCustomThumb() || !lite ) // No MIME type icons #7528
			{
				links.put("thumbnail", linkFactory.createThumbnailAttachmentLink(id, uuid).getHref());
			}

			if( lite )
			{
				continue;
			}

			// May as well do this here while we have the viewable resource
			// together
			List<AttachmentDetail> details = vr.getCommonAttachmentDetails();
			Map<String, String> detailsAsStrings = new HashMap<String, String>();

			for( AttachmentDetail detail : details )
			{
				if( !(detail.getDescription() instanceof LabelRenderer) )
				{
					// Label renderer can be be converted to string value, if
					// it's anything else don't bother, not worth the trouble
					continue;
				}
				// There might be a better way, but this should work (should)
				detailsAsStrings.put(detail.getName().getText(), detail.getDescription().toString());
			}
			attachment.set("details", detailsAsStrings);

			final String type = attachment.getAttachmentType();
			if( type == null || !attachment.isPreview() )
			{
				continue;
			}

			String href = null;

			// More file types??
			if( type.equalsIgnoreCase(AttachmentType.FILE.toString())
				|| type.equalsIgnoreCase(AttachmentType.ZIP.toString()) )
			{
				links.put(
					"view",
					urlService.institutionalise(PathUtils.filePath("api/store/catalogue", catalogueUuid, "item",
						id.getUuid(), "attachment", uuid)));
				continue;
			}

			if( vr.isExternalResource() )
			{
				attachment.setExternal(true);
				attachment.setAttachmentType(AttachmentType.LINK.toString().toLowerCase());
				try
				{
					href = vr.createCanonicalUrl().getHref();
					links.put("view", href);
				}
				catch( SectionsRuntimeException s )
				{
					// The vr doesn't have a canonurl, don't show as a preview
				}
			}

			if( Check.isEmpty(href) )
			{
				attachment.setPreview(false);
				attachment.setAttachmentType(UNSUPPORTED);
			}
		}
	}

	private SectionInfo getInfo()
	{
		// I think this is ok??
		return sectionsController.createInfo("/viewitem/viewitem.do", null, null, null, null, null);
	}
}
