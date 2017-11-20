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

package com.tle.admin.powersearch;

import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.TableLayout;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.admin.gui.common.actions.AddAction;
import com.tle.admin.gui.common.actions.DownAction;
import com.tle.admin.gui.common.actions.JTextlessButton;
import com.tle.admin.gui.common.actions.RemoveAction;
import com.tle.admin.gui.common.actions.TLEAction;
import com.tle.admin.gui.common.actions.UpAction;
import com.tle.client.gui.popup.TablePopupListener;
import com.tle.common.i18n.CurrentLocale;

public class PowerSearchList extends JPanel implements Changeable
{
	private static final long serialVersionUID = 1L;
	private ControlRepository repository;
	private ActionListener addControlListener = null;

	private String originalXml = null;
	private ChangeDetector changeDetector;
	private JTable table;
	private PowerSearchModel model;
	private List<TLEAction> actions;

	public PowerSearchList(ControlRepository repository)
	{
		this.repository = repository;
		setup();
	}

	private void setup()
	{
		actions = new ArrayList<TLEAction>();
		actions.add(upAction);
		actions.add(downAction);
		actions.add(addAction);
		actions.add(removeAction);

		model = new PowerSearchModel(repository);
		table = new JTable(model)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean getScrollableTracksViewportHeight()
			{
				// fetch the table's parent
				Container viewport = getParent();

				// if the parent is not a viewport, calling this isn't useful
				if( !(viewport instanceof JViewport) )
				{
					return false;
				}

				// return true if the table's preferred height is smaller
				// than the viewport height, else false
				return getPreferredSize().height < viewport.getHeight();
			}
		};
		table.setShowGrid(false);
		table.addMouseListener(new TablePopupListener(table, actions));
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if( !e.getValueIsAdjusting() )
				{
					updateButtons();
				}
			}
		});

		TableColumn c = table.getColumnModel().getColumn(1);
		c.setMinWidth(50);
		c.setMaxWidth(50);
		c.setWidth(50);

		JScrollPane tableScroll = new JScrollPane(table);

		JButton addButton = new JButton(addAction);
		JButton removeButton = new JButton(removeAction);
		JButton upButton = new JTextlessButton(upAction);
		JButton downButton = new JTextlessButton(downAction);

		final int height1 = upButton.getPreferredSize().height;
		final int height2 = removeButton.getPreferredSize().height;
		final int width1 = upButton.getPreferredSize().width;
		final int width2 = removeButton.getPreferredSize().width;

		final int[] rows = {TableLayout.FILL, height1, height1, TableLayout.FILL, height2,};
		final int[] cols = {width1, TableLayout.FILL, width2, width2, TableLayout.FILL,};

		setLayout(new TableLayout(rows, cols, 5, 5));

		add(tableScroll, new Rectangle(1, 0, 4, 4));
		add(upButton, new Rectangle(0, 1, 1, 1));
		add(downButton, new Rectangle(0, 2, 1, 1));
		add(addButton, new Rectangle(2, 4, 1, 1));
		add(removeButton, new Rectangle(3, 4, 1, 1));

		updateButtons();

		changeDetector = new ChangeDetector();
		changeDetector.watch(model);
	}

	public void addListSelectionListener(ListSelectionListener listener)
	{
		table.getSelectionModel().addListSelectionListener(listener);
	}

	public void setAddControlListener(ActionListener listener)
	{
		addControlListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#hasDetectedChanges()
	 */
	@Override
	public boolean hasDetectedChanges()
	{
		if( changeDetector.hasDetectedChanges() || originalXml == null )
		{
			return true;
		}
		else
		{
			String newXml = WizardHelper.getXmlForComparison(model.getSearchPage());
			if( !originalXml.equals(newXml) )
			{
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.Changeable#clearChanges()
	 */
	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
		originalXml = WizardHelper.getXmlForComparison(model.getSearchPage());
	}

	private void updateButtons()
	{
		for( TLEAction action : actions )
		{
			action.update();
		}
	}

	public Control getRootControl()
	{
		return model.getRootControl();
	}

	public Control getSelectedControl()
	{
		if( table.getSelectedRowCount() > 0 )
		{
			int row = table.getSelectedRow();
			return model.getControlAt(row);
		}
		else
		{
			return null;
		}
	}

	public void setSelectedControl(Control control)
	{
		if( control != null )
		{
			int row = model.getRowOfControl(control);
			// check control is found (might not be displayed to user)
			if( row >= 0 )
			{
				table.setRowSelectionInterval(row, row);
				return;
			}
		}
		table.clearSelection();
	}

	public int getControlCount()
	{
		return model.getRowCount();
	}

	public List<Control> getControls()
	{
		List<Control> results = new ArrayList<Control>();
		for( int i = 0; i < getControlCount(); i++ )
		{
			results.add(model.getControlAt(i));
		}
		return results;
	}

	public void controlChanged(Control control)
	{
		model.controlChanged(control);
	}

	public void addControl(ControlDefinition definition)
	{
		model.addControl(definition);
	}

	public DefaultWizardPage getSearchPage()
	{
		return model.getSearchPage();
	}

	public void load(DefaultWizardPage page)
	{
		table.clearSelection();
		model.loadWizard(page);
		table.updateUI();
		updateButtons();
		clearChanges();
	}

	private TLEAction addAction = new AddAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if( addControlListener != null )
			{
				e.setSource(PowerSearchList.this);
				addControlListener.actionPerformed(e);
			}
		}
	};

	private TLEAction removeAction = new RemoveAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Control c = getSelectedControl();
			if( c != null )
			{
				int confirm = JOptionPane.showConfirmDialog(PowerSearchList.this,
					CurrentLocale.get("com.dytech.edge.admin.wizard.powersearchlist.confirm"), CurrentLocale //$NON-NLS-1$
						.get("com.dytech.edge.admin.wizard.powersearchlist.remove"), //$NON-NLS-1$
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

				if( confirm == JOptionPane.YES_OPTION )
				{
					table.clearSelection();
					model.removeControl(c);
				}
			}
		}

		@Override
		public void update()
		{
			setEnabled(table.getSelectedRowCount() > 0);
		}
	};

	private TLEAction upAction = new UpAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Control c = getSelectedControl();
			if( c != null )
			{
				setSelectedControl(null);
				model.raiseControl(c);
				setSelectedControl(c);
			}
		}

		@Override
		public void update()
		{
			setEnabled(table.getSelectedRowCount() != 0 && table.getSelectedRow() != 0);
		}
	};

	private TLEAction downAction = new DownAction()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			Control c = getSelectedControl();
			if( c != null )
			{
				setSelectedControl(null);
				model.lowerControl(c);
				setSelectedControl(c);
			}
		}

		@Override
		public void update()
		{
			setEnabled(table.getSelectedRowCount() > 0 && table.getSelectedRow() != model.getRowCount() - 1);
		}
	};

	public void clearSelection()
	{
		table.clearSelection();
	}
}
