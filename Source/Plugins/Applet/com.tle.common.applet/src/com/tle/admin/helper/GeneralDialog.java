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

package com.tle.admin.helper;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.dytech.gui.ComponentHelper;
import com.tle.common.i18n.CurrentLocale;

public abstract class GeneralDialog implements ActionListener
{
	public static final int OK_RESPONSE = 0;
	public static final int CANCEL_RESPONSE = 1;

	protected static final String QUESTION_MARK = "/icons/question.gif";

	protected JPanel inner;
	protected int response;
	protected Object value;
	protected JButton okButton;
	protected JButton cancelButton;

	private String title;
	private Component parent;
	private JComponent content;
	protected JDialog dialog;
	private Dimension dialogSize;

	public GeneralDialog(Component parent, String title)
	{
		this.parent = parent;
		this.title = title;

		setupGUI();
	}

	public void showDialog()
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setContentPane(content);
		dialog.setModal(true);

		if( dialogSize != null )
		{
			dialog.setSize(dialogSize);
		}

		dialog.setVisible(true);
	}

	public int getResponse()
	{
		return response;
	}

	public Object getValue()
	{
		return value;
	}

	public void setSize(int width, int height)
	{
		dialogSize = new Dimension(width, height);
	}

	protected void setInner(Component c)
	{
		inner.removeAll();
		inner.add(c);
		inner.updateUI();
	}

	protected abstract void cancelled();

	protected abstract void ok();

	protected void setValue(Object o)
	{
		value = o;
	}

	protected void setupGUI()
	{
		inner = new JPanel();
		inner.setLayout(new GridLayout(1, 1));

		okButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.ok"));
		cancelButton = new JButton(CurrentLocale.get("com.dytech.edge.admin.helper.cancel"));

		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		JLabel heading = new JLabel("<html><b>" + title + "</b>");
		heading.setAlignmentY(Component.BOTTOM_ALIGNMENT);

		JPanel top = new JPanel();
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.add(Box.createRigidArea(new Dimension(5, 0)));
		top.add(new JLabel(new ImageIcon(GeneralDialog.class.getResource(QUESTION_MARK))));
		top.add(Box.createRigidArea(new Dimension(5, 0)));
		top.add(heading);
		top.add(Box.createHorizontalGlue());

		JPanel bottom = new JPanel();
		bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
		bottom.add(Box.createHorizontalGlue());
		bottom.add(okButton);
		bottom.add(Box.createRigidArea(new Dimension(5, 0)));
		bottom.add(cancelButton);
		bottom.setMaximumSize(bottom.getPreferredSize());

		JPanel middle = new JPanel();
		middle.setLayout(new BoxLayout(middle, BoxLayout.X_AXIS));
		middle.add(Box.createRigidArea(new Dimension(37, 0)));
		middle.add(inner);
		middle.add(Box.createRigidArea(new Dimension(5, 0)));

		content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		content.add(top);
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		content.add(middle);
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		content.add(bottom);
		content.add(Box.createRigidArea(new Dimension(0, 5)));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == cancelButton )
		{
			onCancel();
		}
		else if( e.getSource() == okButton )
		{
			onOk();
		}
	}

	protected void onCancel()
	{
		response = CANCEL_RESPONSE;
		cancelled();
		dialog.dispose();
	}

	protected void onOk()
	{
		response = OK_RESPONSE;
		ok();
		dialog.dispose();
	}
}
