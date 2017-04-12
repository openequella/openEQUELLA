package com.dytech.installer.controls;

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;
import com.dytech.installer.Item;

public class GEditBox extends GuiControl
{
	protected JTextField field;

	public GEditBox(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public String getSelection()
	{
		return field.getText();
	}

	@Override
	public JComponent generateControl()
	{
		field = new JTextField();
		field.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));

		if( items.size() >= 1 )
		{
			field.setText(((Item) items.get(0)).getValue());
		}

		return field;
	}
}
