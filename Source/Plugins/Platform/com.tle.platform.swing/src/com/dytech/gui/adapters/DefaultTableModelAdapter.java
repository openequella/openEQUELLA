/*
 * Created on Dec 23, 2004
 */
package com.dytech.gui.adapters;

import java.util.List;

import javax.swing.table.DefaultTableModel;

/**
 * @author Nicholas Read
 */
class DefaultTableModelAdapter implements TablePasteModel
{
	private DefaultTableModel model;

	public DefaultTableModelAdapter(DefaultTableModel model)
	{
		this.model = model;
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.adapters.TablePasteModelAdapter#insertRow(int,
	 * java.util.Vector)
	 */
	@Override
	public void insertRow(int row, List<?> data)
	{
		model.insertRow(row, data.toArray());
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.adapters.TablePasteModelAdapter#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int column)
	{
		return model.getColumnClass(column);
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.adapters.TablePasteModelAdapter#getColumnCount()
	 */
	@Override
	public int getColumnCount()
	{
		return model.getColumnCount();
	}

	/*
	 * (non-Javadoc)
	 * @see com.dytech.gui.adapters.TablePasteModelAdapter#getRowCount()
	 */
	@Override
	public int getRowCount()
	{
		return model.getRowCount();
	}
}