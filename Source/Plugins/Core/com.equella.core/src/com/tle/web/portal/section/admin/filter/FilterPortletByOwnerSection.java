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

package com.tle.web.portal.section.admin.filter;

import com.tle.web.portal.section.admin.PortletSearchEvent;
import com.tle.web.search.filter.AbstractFilterByUserSection;
import com.tle.web.sections.SectionInfo;

public class FilterPortletByOwnerSection extends AbstractFilterByUserSection<PortletSearchEvent>
{
	@Override
	public void prepareSearch(SectionInfo info, PortletSearchEvent event) throws Exception
	{
		event.filterByOwner(getSelectedUserId(info));
	}

	@Override
	protected String getPublicParam()
	{
		return "owner"; //$NON-NLS-1$
	}

	@Override
	public String getAjaxDiv()
	{
		return "owner"; //$NON-NLS-1$
	}

}