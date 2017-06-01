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

package com.tle.web.copyright.section;

import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.statement.ScriptStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;
import com.tle.web.wizard.WizardExceptionHandler;

@SuppressWarnings("nls")
@Bind
public class CopyrightViolationHandler extends AbstractPrototypeSection<CopyrightViolationHandler.CalViolationModel>
	implements
		WizardExceptionHandler
{
	@PlugKey("violation.title")
	private static Label TITLE_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component
	@PlugKey("violation.button.cancel")
	private Button cancelButton;

	@Override
	public SectionResult handleException(SectionInfo info, Throwable e)
	{
		CopyrightViolationHandler violationHandler = info.lookupSection(CopyrightViolationHandler.class);
		return violationHandler.renderException(info, (CopyrightViolationException) e); // NOSONAR
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		cancelButton.setClickHandler(new StatementHandler(new ScriptStatement("history.back();")));
	}

	public SectionResult renderException(SectionInfo info, CopyrightViolationException we)
	{
		Decorations.getDecorations(info).setTitle(TITLE_LABEL);

		CalViolationModel model = getModel(info);
		model.setException(we);
		return viewFactory.createResult("violation.ftl", this);
	}

	public static class CalViolationModel
	{
		private CopyrightViolationException exception;

		public CopyrightViolationException getException()
		{
			return exception;
		}

		public void setException(CopyrightViolationException exception)
		{
			this.exception = exception;
		}
	}

	@Override
	public Class<CalViolationModel> getModelClass()
	{
		return CalViolationModel.class;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}
}
