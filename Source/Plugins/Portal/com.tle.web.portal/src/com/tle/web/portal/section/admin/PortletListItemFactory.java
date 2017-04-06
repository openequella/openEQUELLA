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
