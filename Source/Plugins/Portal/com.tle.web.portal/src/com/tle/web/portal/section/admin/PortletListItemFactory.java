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

package com.tle.web.portal.section.admin;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.js.JSHandler;

/**
 * @author aholland
 */
@Bind
@Singleton
public class PortletListItemFactory
{
	@Inject
	private Provider<PortletListItem> itemFactory;
	@Inject
	private UserLinkService userLinkService;
	private UserLinkSection userLinkSection;

	public void register(String id, SectionTree tree)
	{
		userLinkSection = userLinkService.register(tree, id);
	}

	public PortletListItem createPortletListItem(SectionInfo info, Portlet portlet, JSHandler editHandler,
		JSHandler deleteHandler)
	{
		PortletListItem listItem = itemFactory.get();
		listItem.setEditHandler(editHandler);
		listItem.setDeleteHandler(deleteHandler);
		listItem.setPortlet(portlet);
		listItem.setOwnerLabel(userLinkSection.createLink(info, portlet.getOwner()));
		return listItem;
	}
}
