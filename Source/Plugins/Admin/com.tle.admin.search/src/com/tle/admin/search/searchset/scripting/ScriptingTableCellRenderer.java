package com.tle.admin.search.searchset.scripting;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;

/**
 * @author Nicholas Read
 */
public class ScriptingTableCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;
	private final ScriptingTableModelInterface<?> model;

	public ScriptingTableCellRenderer(ScriptingTableModelInterface<?> model)
	{
		this.model = model;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if( Check.isEmpty((String) value) )
		{
			setText(CurrentLocale.get("com.tle.admin.gui.common.cell.scriptingtablecellrenderer.add"));
		}
		else
		{
			setText(CurrentLocale.get("com.tle.admin.gui.common.cell.scriptingtablecellrenderer.edit"));
		}
		setEnabled(model.isScriptingEnabled(row));
		return this;
	}
}