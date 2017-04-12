package com.tle.core.portal.dao;

import java.util.Collection;
import java.util.List;

import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;

/**
 * @author aholland
 */
public interface PortletPreferenceDao extends GenericInstitutionalDao<PortletPreference, Long>
{
	PortletPreference getForPortlet(String userId, Portlet portlet);

	/**
	 * Generally only useful in an export preferences context
	 */
	List<PortletPreference> getAllForPortlet(final Portlet portlet);

	List<PortletPreference> getForPortlets(String userId, Collection<Portlet> portlets);

	int deleteAllForPortlet(Portlet portlet);

	void deleteAllForUser(String userId);

	void changeUserId(String fromUserId, String toUserId);
}
