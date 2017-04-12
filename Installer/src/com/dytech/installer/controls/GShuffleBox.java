package com.dytech.installer.controls;

import java.awt.Component;

import javax.swing.JComponent;

import com.dytech.devlib.PropBagEx;
import com.dytech.gui.JShuffleBox;
import com.dytech.installer.InstallerException;

public class GShuffleBox extends GuiControl
{
	protected JShuffleBox box;

	public GShuffleBox(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	@Override
	public String getSelection()
	{
		StringBuilder buff = new StringBuilder();

		int count = box.getRightCount();
		for( int i = 0; i < count; i++ )
		{
			buff.append(box.getRightAt(i).toString());
			buff.append(", ");
		}

		if( buff.length() == 0 )
			return buff.toString();
		else
			return buff.substring(0, buff.lastIndexOf(","));
	}

	@Override
	public JComponent generateControl()
	{
		box = new JShuffleBox();
		box.addToLeft(items);
		box.setAlignmentX(Component.LEFT_ALIGNMENT);

		return box;
	}
}