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

import com.tle.annotation.NonNullByDefault;
import com.tle.cal.web.service.CALWebServiceImpl;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.service.CopyrightService;
import com.tle.core.guice.Bind;
import com.tle.web.copyright.section.AbstractCopyrightAgreementDialog;
import com.tle.web.copyright.section.AbstractCopyrightAgreementSection;
import com.tle.web.copyright.service.CopyrightWebService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author Aaron
 */
@NonNullByDefault
@TreeIndexed
@Bind
public class CALAgreementSection extends AbstractCopyrightAgreementSection
{
	@Inject
	private CALWebServiceImpl calService;

	@ViewFactory
	private FreemarkerFactory view;

	@Inject
	@Component
	private CALAgreementDialog agreementDialog;

	@Override
	protected AbstractCopyrightAgreementDialog getDialog()
	{
		return agreementDialog;
	}

	@SuppressWarnings("nls")
	@Override
	protected SectionRenderable getStandardAgreement(RenderContext info)
	{
		return view.createResult("stdagreement.ftl", this);
	}

	@Override
	protected CopyrightWebService<? extends Holding> getCopyrightWebServiceImpl()
	{
		return calService;
	}

	@Override
	protected CopyrightService<? extends Holding, ? extends Portion, ? extends com.tle.core.copyright.Section> getCopyrightServiceImpl()
	{
		return calService.getCopyrightServiceImpl();
	}
}
