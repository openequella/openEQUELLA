package com.dytech.installer.controls;

import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.InstallerException;

public class GFileSelector extends GResourceSelector
{
	public GFileSelector(PropBagEx controlBag) throws InstallerException
	{
		super(controlBag);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle(title);

		int returnVal = chooser.showOpenDialog(field);
		if( returnVal == JFileChooser.APPROVE_OPTION )
		{
			field.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}
}
