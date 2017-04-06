package com.dytech.edge.admin.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

import com.tle.common.i18n.CurrentLocale;

public class ReloadHandler implements ActionListener
{
	private JCheckBox reload = null;

	public ReloadHandler(JCheckBox refresh)
	{
		this.reload = refresh;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( reload.isSelected() )
		{
			int result = JOptionPane.showConfirmDialog(reload,
				CurrentLocale.get("com.dytech.edge.admin.wizard.reloadhandler.selecting"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.reloadhandler.warning"), JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);

			if( result == JOptionPane.NO_OPTION )
			{
				reload.setSelected(false);
			}
		}
	}
}
