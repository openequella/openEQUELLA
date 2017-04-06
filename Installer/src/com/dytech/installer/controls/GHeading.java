package com.dytech.installer.controls;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;

public class GHeading extends GuiControl
{
	public GHeading(PropBagEx controlBag) throws InstallerException
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
		JLabel label = new JLabel(title);
		label.setFont(new Font("Arial", Font.BOLD, 30));

		panel.add(label);
	}

	@Override
	public JComponent generateControl()
	{
		return new JPanel();
	}
}
