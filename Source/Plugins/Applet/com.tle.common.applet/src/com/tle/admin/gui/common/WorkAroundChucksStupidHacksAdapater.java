package com.tle.admin.gui.common;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dytech.gui.Changeable;

/**
 * @author Nicholas Read
 */
public class WorkAroundChucksStupidHacksAdapater extends JPanel implements Changeable
{
	private static final long serialVersionUID = 1L;
	private final Changeable changeable;

	public WorkAroundChucksStupidHacksAdapater(JComponent component, Changeable changeable)
	{
		this.changeable = changeable;

		setLayout(new GridLayout(1, 1));
		add(component);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#clearChanges()
	 */
	@Override
	public void clearChanges()
	{
		changeable.clearChanges();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#hasDetectedChanges()
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		return changeable.hasDetectedChanges();
	}
}