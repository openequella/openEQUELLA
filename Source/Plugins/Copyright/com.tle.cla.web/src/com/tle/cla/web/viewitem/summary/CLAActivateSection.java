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

package com.tle.cla.web.viewitem.summary;

import javax.inject.Inject;

import com.tle.cla.CLAConstants;
import com.tle.cla.web.service.CLAWebServiceImpl;
import com.tle.core.copyright.Holding;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractActivateSection;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
@Bind
public class CLAActivateSection extends AbstractActivateSection
{
	@Inject
	private CLAWebServiceImpl claWebService;

	@Override
	protected String getActivationType()
	{
		return CLAConstants.ACTIVATION_TYPE;
	}

	@Override
	protected CopyrightWebService<? extends Holding> getCopyrightServiceImpl()
	{
		return claWebService;
	}
}
