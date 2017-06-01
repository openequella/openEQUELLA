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

package com.tle.cal.web.viewitem.summary;

import javax.inject.Inject;

import com.tle.cal.CALConstants;
import com.tle.cal.web.service.CALWebServiceImpl;
import com.tle.core.copyright.Holding;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractActivateSection;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
@Bind
public class CALActivateSection extends AbstractActivateSection
{
	@Inject
	private CALWebServiceImpl calWebService;
	@Inject
	private CALPercentageOverrideSection overrideSection;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerSections(overrideSection, id);
	}

	@Override
	protected CALPercentageOverrideSection getOverrideSection()
	{
		return overrideSection;
	}

	@Override
	protected String getActivationType()
	{
		return CALConstants.ACTIVATION_TYPE;
	}

	@Override
	protected CopyrightWebService<? extends Holding> getCopyrightServiceImpl()
	{
		return calWebService;
	}
}
