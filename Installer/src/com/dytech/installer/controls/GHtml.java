package com.dytech.installer.controls;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;

public class GHtml extends GuiControl
{
	public GHtml(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public String getSelection()
	{
		return new String();
	}

	@Override
	public void generate(JPanel panel)
	{
		panel.add(new JLabel(title));
	}

	@Override
	public JComponent generateControl()
	{
		return new JPanel();
	}
}
