/*
 * Created on Jun 22, 2005
 */
package com.dytech.edge.wizard.beans.control;

import com.tle.beans.entity.LanguageBundle;

public class Repeater extends AbstractControlsWizardControl
{
	private static final long serialVersionUID = 1;
	public static final String REPEATER_CLASS = "repeater";

	private LanguageBundle noun;
	private int min;
	private int max;

	public Repeater()
	{
		min = 1;
		max = 10;
	}

	@Override
	public String getClassType()
	{
		return REPEATER_CLASS;
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		this.max = max;
	}

	public int getMin()
	{
		return min;
	}

	public void setMin(int min)
	{
		this.min = min;
	}

	public LanguageBundle getNoun()
	{
		return noun;
	}

	public void setNoun(LanguageBundle noun)
	{
		this.noun = noun;
	}
}
