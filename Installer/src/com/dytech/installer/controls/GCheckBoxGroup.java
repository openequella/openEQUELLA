package com.dytech.installer.controls;

import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public class GCheckBoxGroup extends GAbstractButtonGroup
{
	public GCheckBoxGroup(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.controls.GAbstractButtonGroup#generateButton(
	 * java.lang.String, javax.swing.ButtonGroup)
	 */
	@Override
	public AbstractButton generateButton(String name, ButtonGroup group)
	{
		return new JCheckBox(name);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.dytech.installer.controls.GuiControl#saveToTargets(com.dytech.devlib
	 * .PropBagEx)
	 */
	@Override
	public void saveToTargets(PropBagEx outputBag)
	{
		Iterator i = targets.iterator();
		while( i.hasNext() )
		{
			String baseTarget = (String) i.next();
			Iterator j = items.iterator();
			while( j.hasNext() )
			{
				Item item = (Item) j.next();

				String target = baseTarget + "/" + item.getValue();
				String value = item.getButton().isSelected() ? "true" : "false";

				outputBag.deleteNode(target);
				outputBag.createNode(target, value);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.controls.GuiControl#getSelection()
	 */
	@Override
	public String getSelection()
	{
		return new String();
	}
}