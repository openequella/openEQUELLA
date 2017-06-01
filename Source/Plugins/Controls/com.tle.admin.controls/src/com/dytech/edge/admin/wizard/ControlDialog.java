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

package com.dytech.edge.admin.wizard;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
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
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author nread
 */
public class ControlDialog implements ActionListener, ListSelectionListener, MouseListener
{
	private ControlDefinition result;
	private ControlRepository repository;

	private JPanel content;
	private JList list;
	private DefaultListModel model;
	private JButton ok;
	private JButton cancel;
	private JDialog dialog;

	/**
	 * Constructs a new ControlDialog.
	 */
	public ControlDialog(ControlRepository repository)
	{
		this.repository = repository;

		setup();
	}

	private void setup()
	{
		JLabel label = new JLabel(getTitle());

		model = new DefaultListModel();
		list = new JList(model);
		list.setCellRenderer(new Renderer(repository));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		list.addMouseListener(this);

		ok = new JButton("OK");
		cancel = new JButton("Cancel");

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

	public void addControls(String category)
	{
		List<ControlDefinition> defs = repository.getDefinitionsForContext(category);
		for( ControlDefinition def : defs )
		{
			model.addElement(def);
		}
	}

	private void updateButtons()
	{
		ok.setEnabled(!list.isSelectionEmpty());
	}

	public ControlDefinition promptForSelection(Component parent)
	{
		dialog = ComponentHelper.createJDialog(parent);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setContentPane(content);
		dialog.setSize(220, 400);
		dialog.setModal(true);
		dialog.setTitle(getTitle());
		dialog.setLocationRelativeTo(parent);
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
			result = (ControlDefinition) model.get(list.getSelectedIndex());
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
				result = (ControlDefinition) model.get(index);
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

	/**
	 * Renders the controls in the list.
	 * 
	 * @author Nicholas Read
	 */
	private static class Renderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 1L;
		private ControlRepository repository;

		public Renderer(ControlRepository repository)
		{
			this.repository = repository;
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			ControlDefinition definition = (ControlDefinition) value;
			setIcon(repository.getIcon(definition.getId(), false));
			return this;
		}
	}

	private static String getTitle()
	{
		return CurrentLocale.get("com.dytech.edge.admin.wizard.controldialog.title");
	}
}
