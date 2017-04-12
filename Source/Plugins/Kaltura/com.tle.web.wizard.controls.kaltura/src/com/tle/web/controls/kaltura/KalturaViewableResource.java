package com.tle.web.controls.kaltura;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.common.base.Throwables;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.common.kaltura.KalturaUtils;
import com.tle.common.kaltura.entity.KalturaServer;
import com.tle.core.kaltura.service.KalturaService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewable.servlet.ThumbServlet.GalleryParameter;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.AbstractWrappedResource;

@SuppressWarnings("nls")
public class KalturaViewableResource extends AbstractWrappedResource
{
	static
	{
		PluginResourceHandler.init(KalturaViewableResource.class);
	}

	@PlugKey("details.type")
	private static Label TYPE;
	@PlugKey("details.mimetype")
	private static Label MIMETYPE;
	@PlugKey("details.title")
	private static Label NAME;
	@PlugKey("details.uploaded")
	private static Label UPLOADED;
	@PlugKey("details.tags")
	private static Label TAGS;

	private final CustomAttachment kalturaAttachment;
	private final KalturaService kalturaService;
	private DateRendererFactory dateRendererFactory;

	public KalturaViewableResource(ViewableResource resource, CustomAttachment attachment, SelectionService selection,
		KalturaService kalturaService, SectionInfo info, DateRendererFactory dateRendererFactory)
	{
		super(resource);
		this.kalturaAttachment = attachment;
		this.dateRendererFactory = dateRendererFactory;

		if( selection.getCurrentSession(info) != null )
		{
			resource.setAttribute(ViewableResource.PREFERRED_LINK_TARGET, "_blank");
		}
		this.kalturaService = kalturaService;
	}

	@Override
	public boolean hasContentStream()
	{
		return false;
	}

	@Override
	public String getMimeType()
	{
		return KalturaUtils.MIME_TYPE;
	}

	@Override
	public boolean isExternalResource()
	{
		return true;
	}

	@Override
	public Bookmark createCanonicalUrl()
	{
		KalturaServer ks = kalturaService.getByUuid((String) kalturaAttachment
			.getData(KalturaUtils.PROPERTY_KALTURA_SERVER));
		String href = MessageFormat.format("{0}/kwidget/wid/_{1}/uiconf_id/{2}/entry_id/{3}", ks.getEndPoint(),
			Integer.toString(ks.getPartnerId()), Integer.toString(ks.getKdpUiConfId()),
			kalturaAttachment.getData(KalturaUtils.PROPERTY_ENTRY_ID));

		return new SimpleBookmark(href);
	}

	@Override
	public ThumbRef getThumbnailReference(SectionInfo info, GalleryParameter gallery)
	{
		try
		{
			return new ThumbRef(new URL(getThumbUrl()));
		}
		catch( MalformedURLException e )
		{
			throw Throwables.propagate(e);
		}
	}

	private String getThumbUrl()
	{
		return (String) kalturaAttachment.getData(KalturaUtils.PROPERTY_THUMB_URL);
	}

	@Override
	public ImageRenderer createStandardThumbnailRenderer(Label alt)
	{
		return new ImageRenderer(getThumbUrl(), alt);
	}

	@Override
	public boolean isCustomThumb()
	{
		return true;
	}

	@Override
	public List<AttachmentDetail> getCommonAttachmentDetails()
	{
		List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

		// Type
		commonDetails.add(makeDetail(TYPE, MIMETYPE));

		// Name (Proper Kaltura video title)
		String name = (String) kalturaAttachment.getData(KalturaUtils.PROPERTY_TITLE);
		if( !Check.isEmpty(name) )
		{
			commonDetails.add(makeDetail(NAME, new TextLabel(name)));
		}

		// Duration is zero before conversion so we cannot store it

		// Author/Uploader does not make sense for videos uploaded from EQUELLA

		// Uploaded
		Long date = (Long) kalturaAttachment.getData(KalturaUtils.PROPERTY_DATE);
		if( date != null )
		{
			commonDetails.add(makeDetail(UPLOADED, dateRendererFactory.createDateRenderer(new Date(date))));
		}

		// Tags
		String tags = (String) kalturaAttachment.getData(KalturaUtils.PROPERTY_TAGS);
		if( !Check.isEmpty(tags) )
		{
			commonDetails.add(makeDetail(TAGS, new TextLabel(tags)));
		}

		return commonDetails;
	}

}
