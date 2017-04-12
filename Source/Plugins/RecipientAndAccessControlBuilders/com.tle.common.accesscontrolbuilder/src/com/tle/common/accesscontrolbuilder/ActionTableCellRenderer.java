package com.tle.common.accesscontrolbuilder;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ActionTableCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;
	private static final String GRANT = CurrentLocale.get("security.editor.grant"); //$NON-NLS-1$
	private static final String REVOKE = CurrentLocale.get("security.editor.revoke"); //$NON-NLS-1$

	public ActionTableCellRenderer()
	{
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if( ((Boolean) value).booleanValue() )
		{
			label.setText(GRANT);
		}
		else
		{
			label.setText(REVOKE);
		}
		return label;
	}
}
