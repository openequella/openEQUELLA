/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.gui.common;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.TableLayout;
import com.dytech.gui.calendar.CalendarDialog;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;

@SuppressWarnings("nls")
public class DateSelector extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final Log LOGGER = LogFactory.getLog(DateSelector.class);

	protected JTextField defaultField;
	protected JButton defaultSelector;
	protected JButton defaultClear;

	/**
	 * Constructs a new DateSelector.
	 */
	public DateSelector()
	{
		init();

		defaultClear.addActionListener(this);
		defaultSelector.addActionListener(this);

		final int width2 = defaultSelector.getPreferredSize().width;
		final int width3 = defaultClear.getPreferredSize().width;
		final int height1 = defaultField.getPreferredSize().height;

		final int[] rows = {height1};
		final int[] cols = {TableLayout.FILL, width2, width3, TableLayout.FILL};

		setLayout(new TableLayout(rows, cols, 5, 5));

		add(defaultField, new Rectangle(0, 0, 1, 1));
		add(defaultSelector, new Rectangle(1, 0, 1, 1));
		add(defaultClear, new Rectangle(2, 0, 1, 1));
	}

	protected void init()
	{
		defaultField = new JTextField();
		defaultField.setEditable(false);
		defaultSelector = new JButton("Select");
		defaultClear = new JButton("Clear");
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		defaultSelector.setEnabled(enabled);
		defaultClear.setEnabled(enabled);
	}

	public Date getDate()
	{
		String s = defaultField.getText();
		if( Check.isEmpty(s) )
		{
			return null;
		}
		try
		{
			return new UtcDate(s, Dates.DATE_ONLY).toDate();
		}
		catch( ParseException e )
		{
			throw new RuntimeException(e);
		}
	}

	public void setDate(Date date)
	{
		if( date != null )
		{
			defaultField.setText(new UtcDate(date).format(Dates.DATE_ONLY));
		}
	}

	public void setSelectText(String text)
	{
		defaultSelector.setText(text);
	}

	public void setClearText(String text)
	{
		defaultClear.setText(text);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();
		if( src.equals(defaultClear) )
		{
			defaultField.setText("");
		}
		else if( src.equals(defaultSelector) )
		{
			UtcDate savedDate = lookForSavedDate();
			java.util.Date selection = CalendarDialog.showCalendarDialog(this, CurrentLocale
				.get("com.dytech.edge.admin.wizard.editor.calendareditor.selectdate"), savedDate == null ? null
				: savedDate.toDate());
			setDate(selection);
		}
	}

	/**
	 * Attempt to load the calendar control with the saved date in the default
	 * field (if any - we expect at least a default Jan 1st 1970). In practice
	 * this will display the year and month. Highlighting the single day in the
	 * control is just too much of a fiddle with Mouse Events etc to be worth
	 * the effort.
	 * 
	 * @return
	 */
	@Nullable
	private UtcDate lookForSavedDate()
	{
		UtcDate retDate = null;
		String oldDateString = defaultField.getText();
		if( !Check.isEmpty(oldDateString) )
		{
			try
			{
				// Dates.MERLOT is MMM dd, yyyy (eg January 1, 1970)
				retDate = new UtcDate(oldDateString, Dates.MERLOT).conceptualDate();
			}
			catch( ParseException e1 )
			{
				try
				{
					// try the deprecated
					retDate = new UtcDate(oldDateString, Dates.DATE_ONLY).conceptualDate();
				}
				catch( ParseException e2 )
				{
					try
					{
						retDate = UtcDate.conceptualDate(oldDateString);
					}
					catch( ParseException e3 )
					{
						LOGGER.warn("Failed to parse date " + oldDateString);
					}
				}
			}
		}
		return retDate;
	}
}
