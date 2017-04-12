package com.tle.web.wizard.controls;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

/**
 * Provides a data model for static HTML controls.
 * 
 * @author Nicholas Read
 */
public class CStaticHTML extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;

	protected String html;

	public CStaticHTML(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		html = CurrentLocale.get(controlBean.getDescription(), "");
	}

	public String getResolvedHtml()
	{
		return evalString(getDescription());
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		// none
	}

	@Override
	public void saveToDocument(PropBagEx itemxml)
	{
		// none
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	@Override
	public void resetToDefaults()
	{
		// none
	}

	@Override
	public void setValues(String... values)
	{
		// Nothing to do
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	public String getHtml()
	{
		return html;
	}
}
