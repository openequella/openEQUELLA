/*
 * Created on Jun 21, 2004 For "The Learning Edge"
 */
package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.Item;

/**
 * @author jmaginnis
 */
@Bind
public class ShuffleBoxWebControl extends AbstractOptionControl
{
	@Component(stateful = false)
	private MultiSelectionList<Item> list;

	@Override
	public MultiSelectionList<Item> getList()
	{
		return list;
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		if( control.getSize1() == 0 )
		{
			control.setSize1(12);
		}

		if( control.getSize2() == 0 )
		{
			control.setSize2(240);
		}
	}

	@Override
	protected String getTemplate()
	{
		setGroupLabellNeeded(true);
		return "shufflebox.ftl"; //$NON-NLS-1$
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return list;
	}
}
