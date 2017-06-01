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
import com.tle.core.activation.ActivationConstants;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.service.AgreementStatus;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.security.TLEAclManager;
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
	@Inject
	private TLEAclManager aclService;

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

			if( status.isInactive()
				&& aclService.filterNonGrantedPrivileges(ActivationConstants.VIEW_INACTIVE_PORTIONS).isEmpty() )
			{
				throw copyrightService.createViolation(item);
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
