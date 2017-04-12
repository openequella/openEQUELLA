/*
 * Created on 30/11/2005
 */
package com.tle.admin.gui.common;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.border.Border;

@Deprecated
public class JFakePanel
{
	protected JChangeDetectorPanel panel;

	public JFakePanel()
	{
		this.panel = new JChangeDetectorPanel();
	}

	public void add(Component comp)
	{
		panel.add(comp);
	}

	public void add(Component comp, Object constaint)
	{
		panel.add(comp, constaint);
	}

	public void setLayout(LayoutManager layout)
	{
		panel.setLayout(layout);
	}

	public void setBorder(Border border)
	{
		panel.setBorder(border);
	}

	public JPanel getComponent()
	{
		return panel;
	}
}
