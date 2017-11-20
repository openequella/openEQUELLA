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

package com.tle.admin.workflow;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.dytech.gui.ComponentHelper;
import com.dytech.gui.TableLayout;
import com.tle.common.gui.models.GenericListModel;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.workflow.node.DecisionNode;
import com.tle.common.workflow.node.ParallelNode;
import com.tle.common.workflow.node.ScriptNode;
import com.tle.common.workflow.node.SerialNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;

/**
 * @author nread
 */
public class StepDialog implements ActionListener, ListSelectionListener, MouseListener
{

	private JPanel content;
	private JList list;
	private GenericListModel<Class<? extends WorkflowNode>> model;
	private JButton ok;
	private JButton cancel;
	private JDialog dialog;

	private Class<? extends WorkflowNode> result;

	/**
	 * Constructs a new ControlDialog.
	 */
	public StepDialog()
	{
		setup();
		addDefaults();
	}

	private void addDefaults()
	{
		model.add(DecisionNode.class);
		model.add(ParallelNode.class);
		model.add(SerialNode.class);
		model.add(WorkflowItem.class);
		model.add(ScriptNode.class);
	}

	private void setup()
	{
		JLabel label = new JLabel(CurrentLocale.get("com.tle.admin.workflow.stepdialog.title"));

		model = new GenericListModel<Class<? extends WorkflowNode>>();
		list = new JList(model);
		list.setCellRenderer(new WorkflowCellRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		list.addMouseListener(this);

		ok = new JButton(CurrentLocale.get("com.tle.admin.ok"));
		cancel = new JButton(CurrentLocale.get("com.tle.admin.cancel"));

		ok.addActionListener(this);
		cancel.addActionListener(this);

		final int height1 = label.getPreferredSize().height;
		final int height2 = ok.getPreferredSize().height;
		final int width1 = cancel.getPreferredSize().width;

		final int[] rows = {height1, TableLayout.FILL, height2,};
		final int[] cols = {TableLayout.FILL, width1, width1,};

		content = new JPanel(new TableLayout(rows, cols));
		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		content.add(label, new Rectangle(0, 0, 3, 1));
		content.add(new JScrollPane(list), new Rectangle(0, 1, 3, 1));
		content.add(ok, new Rectangle(1, 2, 1, 1));
		content.add(cancel, new Rectangle(2, 2, 1, 1));

		updateButtons();
	}

	private void updateButtons()
	{
		ok.setEnabled(!list.isSelectionEmpty());
	}

	public Class<? extends WorkflowNode> promptForSelection(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setContentPane(content);
		dialog.setSize(220, 400);
		dialog.setModal(true);
		dialog.setTitle(CurrentLocale.get("com.tle.admin.workflow.stepdialog.title"));
		dialog.setLocation(parent.getLocationOnScreen());
		dialog.setVisible(true);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == ok )
		{
			result = model.get(list.getSelectedIndex());
			dialog.dispose();
		}
		else if( e.getSource() == cancel )
		{
			dialog.dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
	 * .ListSelectionEvent)
	 */
	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if( e.getSource() == list )
		{
			updateButtons();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if( e.getClickCount() == 2 )
		{
			final int index = list.locationToIndex(e.getPoint());
			if( index > -1 )
			{
				result = model.get(index);
				dialog.dispose();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseExited(MouseEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e)
	{
		// We don't care about this event
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e)
	{
		// We don't care about this event
	}
}
