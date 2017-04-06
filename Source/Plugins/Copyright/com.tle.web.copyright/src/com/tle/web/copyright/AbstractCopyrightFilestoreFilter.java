package com.tle.web.copyright;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.AgreementStatus;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.web.viewitem.FilestoreContentFilter;
import com.tle.web.viewitem.FilestoreContentStream;
import com.tle.web.viewurl.ViewAttachmentUrl;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;

public abstract class AbstractCopyrightFilestoreFilter<H extends Holding, P extends Portion, S extends Section>
	implements
		FilestoreContentFilter
{
	private static final Log LOGGER = LogFactory.getLog(AbstractCopyrightFilestoreFilter.class);

	@Inject
	private ViewItemUrlFactory urlFactory;

	@Override
	public FilestoreContentStream filter(FilestoreContentStream contentStream, HttpServletRequest request,
		HttpServletResponse response) throws IOException
	{
		String filepath = contentStream.getFilepath();

		ItemKey itemKey = contentStream.getItemId();
		CopyrightService<H, P, S> copyrightService = getCopyrightService();
		ItemId itemId = ItemId.fromKey(itemKey);
		Item item = copyrightService.getCopyrightedItem(itemId);
		if( item != null )
		{
			Attachment attachment = copyrightService.getSectionAttachmentForFilepath(item, filepath);
			if( attachment == null )
			{
				return contentStream;
			}

			AgreementStatus status;
			try
			{
				status = copyrightService.getAgreementStatus(item, attachment);
			}
			catch( IllegalStateException bad )
			{
				LOGGER.error("Error getting AgreementStatus", bad); //$NON-NLS-1$
				return contentStream;
			}

			if( status.isNeedsAgreement() )
			{
				// FIXME: This creates /items/ urls, what if they came from
				// /integ/ ?
				ViewItemUrl vurl = urlFactory.createFullItemUrl(itemKey);
				vurl.add(new ViewAttachmentUrl(attachment.getUuid()));
				response.sendRedirect(vurl.getHref());
				return null;
			}
		}
		return contentStream;
	}

	@Override
	public boolean canView(Item i, IAttachment attach)
	{
		CopyrightService<H, P, S> copyrightService = getCopyrightService();
		Item item = copyrightService.getCopyrightedItem(i.getItemId());
		if( item != null )
		{
			AgreementStatus status;
			try
			{
				status = copyrightService.getAgreementStatus(item, attach);
			}
			catch( IllegalStateException bad )
			{

				return false;
			}

			if( status.isNeedsAgreement() )
			{
				return false;
			}
		}
		return true;
	}

	protected abstract CopyrightService<H, P, S> getCopyrightService();
}
