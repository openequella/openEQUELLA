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

import java.util.Arrays;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;

import com.dytech.edge.admin.wizard.WizardModel;
import com.dytech.edge.admin.wizard.model.Control;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class PowerSearchModel extends AbstractTableModel implements TreeModelListener
{
	private static final long serialVersionUID = 1L;
	private WizardModel model;

	public PowerSearchModel(ControlRepository repository)
	{
		model = new WizardModel(repository);
		model.addTreeModelListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount()
	{
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount()
	{
		return model.getChildCount(getRootControl());
	}

	@Override
	public Class<?> getColumnClass(int column)
	{
		if( column == 1 )
		{
			return Boolean.class;
		}
		else
		{
			return super.getColumnClass(column);
		}
	}

	@Override
	public String getColumnName(int column)
	{
		return getColumnNames()[column];
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int row, int column)
	{
		Control control = (Control) model.getChild(getRootControl(), row);
		if( column == 1 )
		{
			return control.isPowerSearchInclude();
		}
		else
		{
			return control.toString();
		}
	}

	@Override
	public void setValueAt(Object value, int row, int column)
	{
		Control control = (Control) model.getChild(getRootControl(), row);
		if( column == 0 )
		{
			control.setCustomName(value.toString());
		}
		else if( column == 1 )
		{
			boolean b = ((Boolean) value).booleanValue();
			control.setPowerSearchInclude(b);
		}
	}

	public Control getRootControl()
	{
		return model.getRootControl();
	}

	public void controlChanged(Control control)
	{
		model.controlChanged(control);
	}

	public DefaultWizardPage getSearchPage()
	{
		return (DefaultWizardPage) model.getRootControl().save();
	}

	public void loadWizard(DefaultWizardPage page)
	{
		model.clearWizard();
		model.loadWizard(page);
	}

	public Control addControl(ControlDefinition definition)
	{
		return model.addControl(getRootControl(), definition);
	}

	public void removeControl(Control control)
	{
		model.removeControl(control);
	}

	public Control getControlAt(int row)
	{
		return (Control) model.getChild(getRootControl(), row);
	}

	public int getRowOfControl(Control control)
	{
		return model.getIndexOfChild(getRootControl(), control);
	}

	public void raiseControl(Control control)
	{
		model.raiseControl(control);
	}

	public void lowerControl(Control control)
	{
		model.lowerControl(control);
	}

	@Override
	public boolean isCellEditable(int row, int column)
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesInserted(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesInserted(TreeModelEvent e)
	{
		int[] indices = e.getChildIndices();
		Arrays.sort(indices);
		fireTableRowsInserted(indices[0], indices[indices.length - 1]);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesRemoved(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesRemoved(TreeModelEvent e)
	{
		int[] indices = e.getChildIndices();
		Arrays.sort(indices);
		fireTableRowsDeleted(indices[0], indices[indices.length - 1]);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeNodesChanged(javax.swing.event
	 * .TreeModelEvent)
	 */
	@Override
	public void treeNodesChanged(TreeModelEvent e)
	{
		int[] indices = e.getChildIndices();
		Arrays.sort(indices);
		fireTableRowsUpdated(indices[0], indices[indices.length - 1]);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.event.TreeModelListener#treeStructureChanged(javax.swing.
	 * event.TreeModelEvent)
	 */
	@Override
	public void treeStructureChanged(TreeModelEvent e)
	{
		fireTableDataChanged();
	}

	private static String[] getColumnNames()
	{
		return new String[]{CurrentLocale.get("com.dytech.edge.admin.wizard.powersearchmodel.control"),
				CurrentLocale.get("com.dytech.edge.admin.wizard.powersearchmodel.search")};
	}
}
