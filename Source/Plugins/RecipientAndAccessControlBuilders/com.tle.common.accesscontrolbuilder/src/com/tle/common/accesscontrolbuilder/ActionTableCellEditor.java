package com.tle.common.accesscontrolbuilder;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ActionTableCellEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 1L;
	private static final String GRANT = CurrentLocale.get("security.editor.grant"); //$NON-NLS-1$
	private static final String REVOKE = CurrentLocale.get("security.editor.revoke"); //$NON-NLS-1$

	public ActionTableCellEditor()
	{
		super(new JComboBox<>(new String[]{GRANT, REVOKE}));
	}

	@Override
	public Object getCellEditorValue()
	{
		return super.getCellEditorValue().equals(GRANT);
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		String newValue = Boolean.TRUE.equals(value) ? GRANT : REVOKE;
		return super.getTableCellEditorComponent(table, newValue, isSelected, row, column);
	}
}