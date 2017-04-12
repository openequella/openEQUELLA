package com.tle.common.accesscontrolbuilder;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.tle.common.recipientselector.ExpressionFormatter;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class ExpressionListCellRenderer extends DefaultListCellRenderer
{
	private static final long serialVersionUID = 1L;
	private ExpressionFormatter formatter;

	public ExpressionListCellRenderer(RemoteUserService userService)
	{
		formatter = new ExpressionFormatter(userService);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
		boolean cellHasFocus)
	{
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		setText(formatter.convertToInfix(getExpression(value)));
		return this;
	}

	public String getExpression(Object value)
	{
		if( value instanceof String )
		{
			return (String) value;
		}
		throw new IllegalStateException("We should not have reached here");
	}
}
