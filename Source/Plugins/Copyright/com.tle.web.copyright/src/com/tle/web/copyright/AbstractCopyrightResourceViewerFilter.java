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

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.copyright.service.AgreementStatus;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.copyright.section.AbstractCopyrightAgreementDialog;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.ResourceViewerFilter;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;

@SuppressWarnings("nls")
public abstract class AbstractCopyrightResourceViewerFilter implements ResourceViewerFilter
{
	private static final Logger LOGGER = Logger.getLogger(AbstractCopyrightResourceViewerFilter.class);

	protected abstract Class<? extends AbstractCopyrightAgreementDialog> getDialogClass();

	@Inject
	private TLEAclManager aclService;

	@Override
	public LinkTagRenderer filterLink(SectionInfo info, LinkTagRenderer viewerTag, ResourceViewer viewer,
		ViewableResource resource)
	{
		IAttachment attach = resource.getAttachment();
		if( attach != null )
		{
			ViewableItem<?> viewableItem = resource.getViewableItem();
			IItem<?> iitem = viewableItem.getItem();
			// TODO: dirty
			if( iitem instanceof Item )
			{
				Item item = (Item) iitem;
				if( !viewableItem.isItemForReal() || !getCopyrightService().isCopyrightedItem(item) )
				{
					return viewerTag;
				}

				AgreementStatus status;
				try
				{
					status = getCopyrightService().getAgreementStatus(item, attach);
				}
				catch( IllegalStateException bad )
				{
					// there is a problem Eg the portion's holding item has been
					// deleted
					// log it, but continue
					LOGGER.error("Error getting AgreementStatus", bad);
					return null;
				}

				if( status.isInactive()
					&& aclService.filterNonGrantedPrivileges(ActivationConstants.VIEW_INACTIVE_PORTIONS).isEmpty() )
				{
					LinkRenderer inactiveTag = new LinkRenderer(new HtmlLinkState(viewerTag.getLinkState().getLabel()));
					inactiveTag.setDisabled(true);
					return inactiveTag;
				}
				else if( status.isNeedsAgreement() )
				{
					ViewItemUrl vurl = viewer.createViewItemUrl(info, resource);
					SectionInfo linkInfo = vurl.getSectionInfo();
					AbstractCopyrightAgreementDialog dialog = linkInfo.lookupSection(getDialogClass());
					return dialog.createAgreementDialog(linkInfo, info, vurl, viewerTag, attach);
				}
			}
		}
		return viewerTag;
	}

	protected abstract CopyrightService<?, ?, ?> getCopyrightService();
}
