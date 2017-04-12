package com.dytech.installer.controls;

import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public abstract class GAbstractButtonGroup extends GuiControl
{
	public GAbstractButtonGroup(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public void loadControl(PropBagEx xml)
	{
		if( xml != null )
		{
			Iterator i = targets.iterator();
			while( i.hasNext() )
			{
				String target = (String) i.next();
				String value = xml.getNode(target);

				if( items.size() > 0 && value.length() > 0 )
				{
					for( Iterator j = items.iterator(); j.hasNext(); )
					{
						Item item = (Item) j.next();
						boolean selected = item.getValue().equals(value);
						item.setSelected(selected);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.installer.controls.GuiControl#generateControl()
	 */
	@Override
	public JComponent generateControl()
	{
		ButtonGroup group = new ButtonGroup();

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));

		Iterator i = items.iterator();
		while( i.hasNext() )
		{
			Item item = (Item) i.next();

			AbstractButton button = generateButton(item.getName(), group);

			item.setButton(button);
			if( item.isSelected() )
			{
				button.setSelected(true);
			}

			buttons.add(button);
		}

		return buttons;
	}

	protected abstract AbstractButton generateButton(String name, ButtonGroup group);
}
