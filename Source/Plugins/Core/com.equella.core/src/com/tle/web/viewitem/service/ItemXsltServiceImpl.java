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

package com.tle.web.viewitem.service;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.usermanagement.util.UserXmlUtils;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.user.UserService;
import com.tle.core.xslt.service.XsltService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind(ItemXsltService.class)
@Singleton
public class ItemXsltServiceImpl implements ItemXsltService
{
	private static Log LOGGER = LogFactory.getLog(ItemXsltService.class);

	@Inject
	private ItemHelper itemHelper;
	@Inject
	private UserService userService;
	@Inject
	private XsltService xsltService;

	private PluginTracker<ItemXsltExtension> itemXsltTracker;

	@Override
	public String renderSimpleXsltResult(RenderContext info, ItemSectionInfo itemInfo, String xslt)
	{
		String result = executeTransformation(xslt, getXmlForXslt(info, itemInfo));

		List<ItemXsltExtension> itemXsltExtensions = itemXsltTracker.getBeanList();
		for( ItemXsltExtension extension : itemXsltExtensions )
		{
			info.preRender(extension);
		}
		return result;
	}

	private String executeTransformation(String xslt, PropBagEx itemXml)
	{
		try
		{
			return xsltService.transformFromXsltString(xslt, itemXml);
		}
		catch( RuntimeException e )
		{
			LOGGER.error("Error transforming xslt ", e); //$NON-NLS-1$
			return "ERROR running xslt"; //$NON-NLS-1$
		}
	}

	@Override
	public PropBagEx getXmlForXslt(SectionInfo info, ItemSectionInfo itemInfo)
	{
		Item item = itemInfo.getItem();
		PropBagEx itemXml = itemInfo.getItemxml();
		itemXml = itemHelper.convertToXml(new ItemPack(item, itemXml, ""), new ItemHelperSettings( //$NON-NLS-1$
			true));
		String owner = item.getOwner();

		try
		{
			// Retrieve the owner information
			UserBean userBean = userService.getInformationForUser(owner);
			PropBagEx ownerXml = UserXmlUtils.getUserAsXml(userBean);
			itemXml.append("item/owner", ownerXml); //$NON-NLS-1$
		}
		catch( Exception e )
		{
			LOGGER.info("Error including owner '" + owner + "' for xslt"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Include information for our templates and webdav information
		itemXml.setNode("template", "entity/" + item.getItemDefinition().getId() //$NON-NLS-1$ //$NON-NLS-2$
			+ "/displaytemplate/"); //$NON-NLS-1$
		itemXml.setNode("itemdir", itemInfo.getItemdir()); //$NON-NLS-1$
		BookmarkEvent bookmarkEvent = new BookmarkEvent(BookmarkEvent.CONTEXT_SESSION);
		info.processEvent(bookmarkEvent);
		itemXml.setNode("sessionparams", SectionUtils.getParameterString(SectionUtils.getParameterNameValues( //$NON-NLS-1$
			bookmarkEvent.getBookmarkState(), false)));
		itemXml.setNode("collection", CurrentLocale.get(item.getItemDefinition().getName())); //$NON-NLS-1$
		Attachments attachments = new UnmodifiableAttachments(item);
		ImsAttachment ims = attachments.getIms();
		if( ims != null )
		{
			itemXml.setNode("viewims", "viewscorm.jsp"); //$NON-NLS-1$ //$NON-NLS-2$
			itemXml.setNode("imsdir", URLUtils.urlEncode(ims.getUrl()) + '/'); //$NON-NLS-1$
		}

		List<ItemXsltExtension> itemXsltExtensions = itemXsltTracker.getBeanList();
		for( ItemXsltExtension extension : itemXsltExtensions )
		{
			extension.addXml(itemXml, info);
		}
		return itemXml;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		itemXsltTracker = new PluginTracker<ItemXsltExtension>(pluginService, "com.tle.web.viewitem", "itemXslt", null); //$NON-NLS-1$ //$NON-NLS-2$
		itemXsltTracker.setBeanKey("class"); //$NON-NLS-1$
	}
}
