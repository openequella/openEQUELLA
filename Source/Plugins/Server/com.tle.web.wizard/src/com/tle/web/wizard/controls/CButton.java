package com.tle.web.wizard.controls;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.Button;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.common.Check;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

/**
 */
public class CButton extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;
	private boolean pressed;

	public CButton(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		Button thebut = (Button) controlBean;
		String action = thebut.getAction();
		if( !Check.isEmpty(action) )
		{
			int afterind = action.indexOf("//aftersave"); //$NON-NLS-1$
			if( afterind >= 0 )
			{
				thebut.setAfterSaveScript(action.substring(afterind + 11));
			}
		}
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		pressed = false;
	}

	@Override
	public void saveToDocument(PropBagEx itemxml)
	{
		// DO NOTHING
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	@Override
	public void resetToDefaults()
	{
		// DO NOTHING
	}

	@Override
	public void setValues(String... values)
	{
		// DO NOTHING
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public void afterSaveValidate()
	{
		super.afterSaveValidate();
		if( pressed )
		{
			execScript(((Button) controlBean).getAction());
		}
	}

	public void setActionFired(boolean b)
	{
		pressed = b;
	}
}
