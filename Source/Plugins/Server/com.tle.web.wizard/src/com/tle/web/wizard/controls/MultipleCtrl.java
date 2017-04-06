/*
 * Created on Aug 4, 2005
 */
package com.tle.web.wizard.controls;

import java.util.ArrayList;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;

public abstract class MultipleCtrl extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;

	protected List<HTMLControl> controls = new ArrayList<HTMLControl>();

	public MultipleCtrl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	@Override
	public void doEvaluate()
	{
		super.doEvaluate();
		for( HTMLControl control : controls )
		{
			control.evaluate();
		}
	}

	@Override
	public void resetToDefaults()
	{
		for( HTMLControl control : controls )
		{
			control.resetToDefaults();
		}
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		for( HTMLControl control : controls )
		{
			control.loadFromDocument(itemxml);
		}
	}

	@Override
	public void saveToDocument(PropBagEx itemxml) throws Exception
	{
		for( HTMLControl control : controls )
		{
			control.saveToDocument(itemxml);
		}
	}

	@Override
	public void clearInvalid()
	{
		super.clearInvalid();
		for( HTMLControl control : controls )
		{
			control.clearInvalid();
		}
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	public List<HTMLControl> getControls()
	{
		return controls;
	}

	@Override
	public boolean isEmpty()
	{
		for( HTMLControl control : controls )
		{
			if( control.isEmpty() )
			{
				return true;
			}
		}
		return false;
	}
}
