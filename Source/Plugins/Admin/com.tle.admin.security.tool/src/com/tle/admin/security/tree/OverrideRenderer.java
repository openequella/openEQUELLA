/*
 * Created on 2/05/2006
 */
package com.tle.admin.security.tree;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableCellRenderer;

public class OverrideRenderer extends JCheckBox implements TableCellRenderer, UIResource
{
	private static final long serialVersionUID = 1L;
	private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	public OverrideRenderer()
	{
		setHorizontalAlignment(SwingConstants.CENTER);
		setBorderPainted(true);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		setEnabled(table.getModel().isCellEditable(row, column));

		if( isSelected )
		{
			setForeground(table.getSelectionForeground());
			super.setBackground(table.getSelectionBackground());
		}
		else
		{
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		setSelected((value != null && ((Boolean) value).booleanValue()));

		if( hasFocus )
		{
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder")); //$NON-NLS-1$
		}
		else
		{
			setBorder(noFocusBorder);
		}

		return this;
	}
}