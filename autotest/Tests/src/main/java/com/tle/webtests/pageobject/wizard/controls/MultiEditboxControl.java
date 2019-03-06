package com.tle.webtests.pageobject.wizard.controls;

import org.openqa.selenium.WebElement;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.generic.component.MultiLingualEditbox;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;

public class MultiEditboxControl extends AbstractWizardControl<MultiEditboxControl>
{
	private final MultiLingualEditbox editbox;

	public MultiEditboxControl(PageContext context, int ctrlnum, AbstractWizardControlPage<?> page)
	{
		super(context, ctrlnum, page);
		editbox = new MultiLingualEditbox(context, page.getControlId(ctrlnum) + "_multiEdit");
	}

	@Override
	protected WebElement findLoadedElement()
	{
		return editbox.findLoadedElement();
	}

	public MultiLingualEditbox getEditbox()
	{
		return editbox;
	}
}
