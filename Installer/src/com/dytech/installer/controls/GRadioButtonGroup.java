package com.dytech.installer.controls;

import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public class GRadioButtonGroup extends GAbstractButtonGroup
{
	public GRadioButtonGroup(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public AbstractButton generateButton(String name, ButtonGroup group)
	{
		AbstractButton button = new JRadioButton(name);
		group.add(button);
		return button;
	}

	@Override
	public String getSelection()
	{
		Iterator i = items.iterator();
		while( i.hasNext() )
		{
			Item item = (Item) i.next();
			if( item.getButton().isSelected() )
				return item.getValue();
		}

		// We should hopefully never reach here.
		// Maybe we should throw an exception?
		return new String();
	}
}