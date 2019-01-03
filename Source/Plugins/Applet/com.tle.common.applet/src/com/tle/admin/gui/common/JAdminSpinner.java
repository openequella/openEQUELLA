/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
