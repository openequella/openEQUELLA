package com.dytech.installer.controls;

import java.awt.Component;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public class GListBox extends GuiControl
{
	protected JComboBox combobox;

	public GListBox(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public JComponent generateControl()
	{
		combobox = new JComboBox(items);
		combobox.setAlignmentX(Component.LEFT_ALIGNMENT);

		Iterator i = items.iterator();
		while( i.hasNext() )
		{
			Item item = (Item) i.next();
			if( item.isSelected() )
			{
				combobox.setSelectedItem(item);
			}
		}

		return combobox;
	}

	@Override
	public void loadControl(PropBagEx xml)
	{
		if( xml != null || targets.isEmpty() )
		{
			String target = (String) targets.get(0);
			String value = xml.getNode(target);
			if( value.length() > 0 )
			{
				Iterator iter = items.iterator();
				while( iter.hasNext() )
				{
					Item item = (Item) iter.next();
					item.setSelected(item.getValue().equals(value));
				}
			}
		}
	}

	@Override
	public String getSelection()
	{
		Item item = (Item) combobox.getSelectedItem();
		return item.getValue();
	}
}