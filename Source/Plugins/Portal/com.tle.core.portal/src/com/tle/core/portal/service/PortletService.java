package com.tle.core.portal.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.common.portal.PortletTypeDescriptor;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.common.portal.service.RemotePortletService;
import com.tle.core.services.entity.AbstractEntityService;

/**
 * @author aholland
 */
public interface PortletService extends AbstractEntityService<PortletEditingBean, Portlet>, RemotePortletService
{
	String ENTITY_TYPE = "PORTLET"; //$NON-NLS-1$

	List<Portlet> getViewablePortlets();

	List<Portlet> getViewableButClosedPortlets();

	PortletSearchResults searchPortlets(PortletSearch search, int offset, int perPage);

	void close(String portletUuid);

	void restore(String portletUuid);

	void restoreAll();

	boolean canModifyAdminFields();

	PortletPreference getPreference(Portlet portlet);

	Map<Portlet, PortletPreference> getPreferences(Collection<Portlet> portlets);

	void savePreferences(Collection<PortletPreference> preferences);

	void savePreference(PortletPreference preference);

	Map<String, PortletTypeDescriptor> mapAllAvailableTypes();

	List<PortletTypeDescriptor> listContributableTypes(boolean admin);

	Portlet getForEdit(String portletUuid);

	List<Portlet> getViewablePortletsForDisplay();

	int countFromFilters(PortletSearch search);
}
