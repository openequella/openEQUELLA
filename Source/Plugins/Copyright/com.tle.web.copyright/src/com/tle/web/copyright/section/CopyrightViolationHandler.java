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
