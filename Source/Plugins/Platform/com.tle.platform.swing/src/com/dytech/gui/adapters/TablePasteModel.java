/*
 * Created on Dec 23, 2004
 */
package com.dytech.gui.adapters;

import java.util.List;

/**
 * @author Nicholas Read
 */
public interface TablePasteModel
{
	int getRowCount();

	int getColumnCount();

	void insertRow(int row, List<?> data);

	Class<?> getColumnClass(int column);
}
