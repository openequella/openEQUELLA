package com.tle.web.wizard.standard.controls;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.component.MultiEditBox;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CMultiEditBox;
import com.tle.web.wizard.render.WizardFreemarkerFactory;

@Bind
public class MultiEditBoxWebControl extends AbstractSimpleWebControl
{
	@ViewFactory
	private WizardFreemarkerFactory viewFactory;

	@Inject
	@Component(stateful = false)
	private MultiEditBox multiEdit;

	private CMultiEditBox editBox;

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		editBox = (CMultiEditBox) control;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		multiEdit.setSize(editBox.getSize2());
		multiEdit.setLangMap(context, editBox.getLangValues());
		addDisabler(context, multiEdit);
		setGroupLabellNeeded(true);
		return viewFactory.createWizardResult(SectionUtils.renderSection(context, multiEdit), context);
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		editBox.setLangValues(multiEdit.getLangMap(info));
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return multiEdit;
	}
}