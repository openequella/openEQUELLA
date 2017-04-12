package com.tle.core.portal.dao;

import java.util.List;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.dao.AbstractEntityDao;
import com.tle.core.portal.service.PortletSearch;

/**
 * @author aholland
 */
public interface PortletDao extends AbstractEntityDao<Portlet>
{
	List<Portlet> getForUser(final String userId);

	List<Portlet> search(PortletSearch search, int offset, int perPage);

	long count(PortletSearch search);
}
