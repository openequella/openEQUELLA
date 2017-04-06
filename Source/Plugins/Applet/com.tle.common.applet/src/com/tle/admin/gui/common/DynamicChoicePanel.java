package com.tle.admin.gui.common;

import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;

public abstract class DynamicChoicePanel<STATE_TYPE> extends JPanel implements Changeable, ActionListener
{
	protected final ChangeDetector changeDetector = new ChangeDetector();
	private final JComponent separator = new JSeparator();
	private String id;

	public DynamicChoicePanel()
	{
		super();
	}

	public DynamicChoicePanel(LayoutManager lm)
	{
		super(lm);
	}

	public final String getId()
	{
		return id;
	}

	public final void setId(String id)
	{
		this.id = id;
	}

	public void choiceSelected()
	{
		// Nothing by default
	}

	public void choiceDeselected()
	{
		// Nothing by default
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	public JComponent getSeparator()
	{
		return separator;
	}

	public abstract void load(STATE_TYPE state);

	public void afterLoad(STATE_TYPE state)
	{
		// Nothing by default
	}

	public abstract void save(STATE_TYPE state);

	public abstract void removeSavedState(STATE_TYPE state);

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// blah
	}
}