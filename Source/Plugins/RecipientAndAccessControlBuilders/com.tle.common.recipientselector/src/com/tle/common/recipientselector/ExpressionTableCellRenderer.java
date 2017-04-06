package com.tle.common.recipientselector;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class ExpressionTableCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 1L;
	private static final String INVALID_EXPRESSION = CurrentLocale.get("security.editor.invalidexpression");

	private ExpressionFormatter formatter;

	public ExpressionTableCellRenderer(RemoteUserService userService)
	{
		formatter = new ExpressionFormatter(userService);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
		int row, int column)
	{
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		try
		{
			setText(formatter.convertToInfix((String) value));
		}
		catch( Exception ex )
		{
			setText(INVALID_EXPRESSION);
		}
		return this;
	}

	@Override
	public String getToolTipText(MouseEvent event)
	{
		return getText();
	}
}
