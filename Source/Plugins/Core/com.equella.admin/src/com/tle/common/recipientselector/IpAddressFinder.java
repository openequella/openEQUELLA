/*
 * Copyright 2017 Apereo
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

package com.tle.common.recipientselector;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import com.dytech.gui.JNumberTextField;
import com.dytech.gui.TableLayout;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class IpAddressFinder extends JPanel implements UserGroupRoleFinder, ActionListener
{
	private static final long serialVersionUID = 1L;

	private EventListenerList eventListenerList;
	private JRadioButton ipAddressRadio;
	private JRadioButton referrerRadio;
	private IpAddressEditor ipAddress;
	private JTextField referrerField;
	private JRadioButton referrerExact;
	private JRadioButton referrerPartial;

	public IpAddressFinder()
	{
		setupGUI();
	}

	@Override
	public void setEnabled(boolean b)
	{
		super.setEnabled(b);

		ipAddressRadio.setEnabled(b);
		referrerRadio.setEnabled(b);

		updateGui();
	}

	@Override
	public void setSingleSelectionOnly(boolean b)
	{
		// Ignore
	}

	@Override
	public synchronized void addFinderListener(FinderListener listener)
	{
		if( eventListenerList == null )
		{
			eventListenerList = new EventListenerList();
		}
		eventListenerList.add(FinderListener.class, listener);
	}

	@Override
	public RecipientFilter getSelectedFilter()
	{
		return ipAddressRadio.isSelected() ? RecipientFilter.IP_ADDRESS : RecipientFilter.HOST_REFERRER;
	}

	@Override
	public List<Object> getSelectedResults()
	{
		Object value = null;
		if( ipAddressRadio.isSelected() )
		{
			value = ipAddress.save();
		}
		else
		{
			value = referrerField.getText().trim();
			if( referrerPartial.isSelected() )
			{
				value = "*" + value + "*";
			}
		}
		return Arrays.asList(value);
	}

	@Override
	public void clearAll()
	{
		referrerField.setText(null);
		ipAddress.clear();
		fireEvent();
	}

	private void setupGUI()
	{
		ipAddressRadio = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.ipaddressfinder.addip"), true);
		referrerRadio = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.ipaddressfinder.addreferrer"));

		ipAddressRadio.addActionListener(this);
		referrerRadio.addActionListener(this);

		ButtonGroup group1 = new ButtonGroup();
		group1.add(ipAddressRadio);
		group1.add(referrerRadio);

		ipAddress = new IpAddressEditor();
		referrerField = new JTextField();
		referrerField.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyTyped(KeyEvent e)
			{
				fireEvent();
			}
		});

		referrerExact = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.ipaddressfinder.onlymatch"), true);
		referrerPartial = new JRadioButton(CurrentLocale.get("com.tle.admin.recipients.ipaddressfinder.match"));

		referrerExact.addActionListener(this);
		referrerPartial.addActionListener(this);

		ButtonGroup group2 = new ButtonGroup();
		group2.add(referrerExact);
		group2.add(referrerPartial);

		final int height1 = ipAddressRadio.getPreferredSize().height;
		final int height2 = ipAddress.getPreferredSize().height;
		final int height3 = referrerField.getPreferredSize().height;

		final int[] rows = {height1, height2, height1, height3, height3, height3, TableLayout.FILL,};
		final int[] cols = {25, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols));
		add(ipAddressRadio, new Rectangle(0, 0, 2, 1));
		add(ipAddress, new Rectangle(1, 1, 1, 1));
		add(referrerRadio, new Rectangle(0, 2, 2, 1));
		add(referrerField, new Rectangle(1, 3, 1, 1));
		add(referrerExact, new Rectangle(1, 4, 1, 1));
		add(referrerPartial, new Rectangle(1, 5, 1, 1));

		updateGui();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		updateGui();
	}

	private void updateGui()
	{
		ipAddress.setEnabled(ipAddressRadio.isEnabled() && ipAddressRadio.isSelected());

		boolean referrerEnabled = referrerRadio.isEnabled() && referrerRadio.isSelected();
		referrerField.setEnabled(referrerEnabled);
		referrerExact.setEnabled(referrerEnabled);
		referrerPartial.setEnabled(referrerEnabled);
	}

	private synchronized void fireEvent()
	{
		boolean valid = false;
		if( ipAddressRadio.isSelected() )
		{
			valid = ipAddress.isAddressValid();
		}
		else
		{
			valid = referrerField.getText().trim().length() > 0;
		}

		FinderEvent event = new FinderEvent();
		event.setSource(this);
		event.setSelectionCount(valid ? 1 : 0);

		for( FinderListener l : eventListenerList.getListeners(FinderListener.class) )
		{
			l.valueChanged(event);
		}
	}

	private class IpAddressEditor extends JPanel implements KeyListener
	{
		private static final long serialVersionUID = 1L;

		private JNumberTextField part1;
		private JNumberTextField part2;
		private JNumberTextField part3;
		private JNumberTextField part4;
		private JNumberTextField mask;

		public IpAddressEditor()
		{
			JLabel addressLabel = new JLabel(CurrentLocale.get("com.tle.admin.recipients.ipaddressfinder.ipaddress"));
			JLabel maskLabel = new JLabel(CurrentLocale.get("com.tle.admin.recipients.ipaddressfinder.subnet"));
			JLabel dot1 = new JLabel(".");
			JLabel dot2 = new JLabel(".");
			JLabel dot3 = new JLabel(".");
			JLabel slash = new JLabel("/");

			addressLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			addressLabel.setHorizontalAlignment(SwingConstants.CENTER);
			maskLabel.setHorizontalTextPosition(SwingConstants.CENTER);
			maskLabel.setHorizontalAlignment(SwingConstants.CENTER);

			part1 = new JNumberTextField(255);
			part2 = new JNumberTextField(255);
			part3 = new JNumberTextField(255);
			part4 = new JNumberTextField(255);
			mask = new JNumberTextField(32);

			part1.addKeyListener(this);
			part2.addKeyListener(this);
			part3.addKeyListener(this);
			part4.addKeyListener(this);
			mask.addKeyListener(this);

			final int width1 = maskLabel.getPreferredSize().width;
			final int width2 = slash.getPreferredSize().width;
			final int height1 = mask.getPreferredSize().height;
			final int height2 = addressLabel.getPreferredSize().height;

			final int[] rows = {height1, height2,};
			final int[] cols = {width1, width2, width1, width2, width1, width2, width1, width2, width1,};

			setLayout(new TableLayout(rows, cols));
			add(part1, new Rectangle(0, 0, 1, 1));
			add(dot1, new Rectangle(1, 0, 1, 1));
			add(part2, new Rectangle(2, 0, 1, 1));
			add(dot2, new Rectangle(3, 0, 1, 1));
			add(part3, new Rectangle(4, 0, 1, 1));
			add(dot3, new Rectangle(5, 0, 1, 1));
			add(part4, new Rectangle(6, 0, 1, 1));
			add(slash, new Rectangle(7, 0, 1, 1));
			add(mask, new Rectangle(8, 0, 1, 1));

			add(addressLabel, new Rectangle(0, 1, 7, 1));
			add(maskLabel, new Rectangle(8, 1, 1, 1));
		}

		@Override
		public void setEnabled(boolean enabled)
		{
			super.setEnabled(enabled);
			for( Component c : getComponents() )
			{
				c.setEnabled(enabled);
			}
		}

		public void clear()
		{
			part1.clear();
			part2.clear();
			part3.clear();
			part4.clear();
			mask.clear();
		}

		public String save()
		{
			StringBuilder builder = new StringBuilder();
			builder.append(part1.getNumber());
			builder.append('.');
			builder.append(part2.getNumber());
			builder.append('.');
			builder.append(part3.getNumber());
			builder.append('.');
			builder.append(part4.getNumber());
			builder.append('/');
			builder.append(mask.getNumber(32));

			return builder.toString();
		}

		public boolean isAddressValid()
		{
			// We don't care about the mask - it defaults to 32
			return part1.getNumber() >= 0 && part2.getNumber() >= 0 && part3.getNumber() >= 0 && part4.getNumber() >= 0;
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			// We don't care about this event
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			// We don't care about this event
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			fireEvent();
		}
	}
}
