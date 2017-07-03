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

package com.tle.web.viewitem.summary.filter;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.exceptions.DRMException;
import com.tle.beans.item.AbstractItemKey;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.DrmService;
import com.tle.core.item.service.ItemService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.viewitem.FilestoreContentFilter;
import com.tle.web.viewitem.FilestoreContentStream;

@Bind
@Singleton
public class DRMContentFilter implements FilestoreContentFilter
{
	@Inject
	private DrmService drmService;
	@Inject
	private ItemService itemService;

	@Override
	public FilestoreContentStream filter(FilestoreContentStream contentStream, HttpServletRequest request,
		HttpServletResponse response) throws IOException
	{
		ItemKey itemId = contentStream.getItemId();
		if( itemId instanceof AbstractItemKey && ((AbstractItemKey) itemId).isDRMApplicable() )
		{
			boolean composition = drmService.isReferredFromDifferentItem(request, itemId);
			if( drmService.requiresAcceptanceCheck(itemId, false, composition) )
			{
				Item item = itemService.getUnsecure(itemId);
				try
				{
					drmService.isAuthorised(item, CurrentUser.getUserState().getIpAddress());
				}
				catch( DRMException ex )
				{
					throw new AccessDeniedException(CurrentLocale.get("com.tle.web.viewitem.drmfilter.drmprotected")); //$NON-NLS-1$
				}
				DrmSettings rights = drmService.requiresAcceptance(item, false, composition);
				if( rights != null && !drmService.havePreviewedThisSession(itemId) )
				{
					throw new AccessDeniedException(CurrentLocale.get("com.tle.web.viewitem.drmfilter.drmprotected")); //$NON-NLS-1$
				}
			}
		}
		return contentStream;
	}

	@Override
	public boolean canView(Item i, IAttachment a)
	{
		ItemKey itemId = i.getItemId();
		if( ((AbstractItemKey) itemId).isDRMApplicable() )
		{
			if( drmService.requiresAcceptanceCheck(itemId, false, false) )
			{
				Item item = itemService.getUnsecure(itemId);
				try
				{
					drmService.isAuthorised(item, CurrentUser.getUserState().getIpAddress());
				}
				catch( DRMException ex )
				{
					return false;
				}
				DrmSettings rights = drmService.requiresAcceptance(item, false, false);
				if( rights != null && !drmService.havePreviewedThisSession(itemId) )
				{
					return false;
				}
			}
		}
		return true;
	}

}
