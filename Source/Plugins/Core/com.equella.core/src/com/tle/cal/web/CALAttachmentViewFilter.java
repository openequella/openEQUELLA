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

package com.tle.cal.web;

import javax.inject.Inject;

import com.tle.beans.cal.CALHolding;
import com.tle.beans.cal.CALPortion;
import com.tle.beans.cal.CALSection;
import com.tle.cal.web.service.CALWebServiceImpl;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.AbstractCopyrightAttachmentFilter;
import com.tle.web.copyright.service.CopyrightWebService;

@Bind
public class CALAttachmentViewFilter extends AbstractCopyrightAttachmentFilter<CALHolding, CALPortion, CALSection>
{

	@Inject
	private CALWebServiceImpl calWebService;

	@Override
	protected CopyrightService<CALHolding, CALPortion, CALSection> getCopyrightServiceImpl()
	{
		return calWebService.getCopyrightServiceImpl();
	}

	@Override
	protected CopyrightWebService<CALHolding> getCopyrightWebServiceImpl()
	{
		return calWebService;
	}

}
