package com.tle.admin.gui.common;

import java.text.ParseException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.dytech.gui.Changeable;

public class JAdminSpinner extends JSpinner implements Changeable
{
	private static final long serialVersionUID = 1L;
	private final SpinnerNumberModel model;
	private boolean changed;

	public JAdminSpinner(int value, int minimum, int maximum, int stepSize)
	{
		model = new SpinnerNumberModel(value, minimum, maximum, stepSize);
		setModel(model);
	}

	public JAdminSpinner(int value, int stepSize)
	{
		model = new SpinnerNumberModel(value, null, null, stepSize);
		setModel(model);
	}

	@Override
	public Object getValue()
	{
		try
		{
			commitEdit();
		}
		catch( ParseException e )
		{
			// IGNORE
		}
		return super.getValue();
	}

	public String getCurrentValue()
	{
		try
		{
			commitEdit();
		}
		catch( ParseException e )
		{
			// IGNORE
		}
		return model.getNumber().toString();
	}

	public void set(int value, int fallback)
	{
		Number min = (Number) model.getMinimum();
		if( min != null )
		{
			int minimum = min.intValue();
			if( value < minimum )
			{
				value = fallback;
				if( value < minimum )
				{
					value = minimum;
				}
			}
		}
		setValue(value);
	}

	/**
	 * Helper method for working with number spinners.
	 */
	public void set(String value, int fallback)
	{
		int number = fallback;
		try
		{
			number = Integer.parseInt(value);
		}
		catch( NumberFormatException ex )
		{
			// 'number' will keep the value of "fallback"
		}
		setValue(number);
	}

	public int getIntValue()
	{
		try
		{
			commitEdit();
		}
		catch( ParseException e )
		{
			// IGNORE
		}
		return model.getNumber().intValue();
	}

	@Override
	public void setValue(Object value)
	{
		super.setValue(value);
		changed = true;
	}

	@Override
	public void clearChanges()
	{
		changed = false;
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changed;
	}
}
