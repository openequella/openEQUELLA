package com.dytech.installer.controls;

import javax.swing.JComponent;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;

public class GParagraph extends GuiControl
{
	public GParagraph(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public String getSelection()
	{
		return new String();
	}

	@Override
	public JComponent generateControl()
	{
		return null;
	}
}
