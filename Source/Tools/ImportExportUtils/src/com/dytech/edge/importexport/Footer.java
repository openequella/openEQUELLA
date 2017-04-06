package com.dytech.edge.importexport;

import java.awt.Dimension;

import javax.swing.JPanel;

import com.dytech.edge.importexport.icons.Icons;
import com.dytech.gui.JImage;

/**
 * @author nread
 */
public class Footer extends JPanel
{
	protected JImage footer;
	protected JImage working;

	public Footer()
	{
		setLayout(null);

		footer = Icons.getFooter();
		working = Icons.getWorking();

		footer.setBounds(0, 0, 350, 21);
		working.setBounds(10, 7, 43, 8);

		setWorking(false);
	}

	public void setWorking(boolean b)
	{
		removeAll();
		if( b )
		{
			add(working);
		}
		add(footer);
		updateUI();
	}

	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(450, 21);
	}
}
