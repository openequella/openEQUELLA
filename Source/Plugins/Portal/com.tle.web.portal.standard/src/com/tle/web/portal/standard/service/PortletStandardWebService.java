package com.tle.web.portal.standard.service;

import java.util.List;

import com.tle.beans.item.Item;
import com.tle.common.portal.entity.impl.PortletRecentContrib;

/**
 * @author aholland
 */
public interface PortletStandardWebService
{
	List<Item> getRecentContributions(PortletRecentContrib portlet);
}
