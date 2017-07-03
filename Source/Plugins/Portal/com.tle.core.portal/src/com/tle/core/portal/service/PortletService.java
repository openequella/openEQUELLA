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

package com.tle.core.portal.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.common.portal.PortletTypeDescriptor;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.PortletPreference;
import com.tle.common.portal.service.RemotePortletService;
import com.tle.core.entity.service.AbstractEntityService;

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
