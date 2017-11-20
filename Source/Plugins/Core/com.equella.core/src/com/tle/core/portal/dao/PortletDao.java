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

package com.tle.core.portal.dao;

import java.util.List;

import com.tle.common.portal.entity.Portlet;
import com.tle.core.entity.dao.AbstractEntityDao;
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
