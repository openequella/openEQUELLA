package com.dytech.edge.admin.wizard;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.tle.admin.gui.i18n.I18nTextField;
import com.tle.beans.entity.LanguageBundle;
import com.tle.i18n.BundleCache;

public class I18nCellEditor extends AbstractCellEditor implements TableCellEditor
{
	private static final long serialVersionUID = 1L;
	private final I18nTextField field;

	public I18nCellEditor()
	{
		field = new I18nTextField(BundleCache.getLanguages())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void initialiseLayout(String layoutConstraints, String cellConstraint, boolean addTextComponent)
			{
				super.initialiseLayout(layoutConstraints, cellConstraint, false);
			}

			@Override
			protected void onPopupClose()
			{
				fireEditingStopped();
			}
		};

		field.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				field.showPopup();
			}
		});

	}

	@Override
	public boolean isCellEditable(EventObject anEvent)
	{
		if( anEvent instanceof MouseEvent )
		{
			return ((MouseEvent) anEvent).getClickCount() >= 2;
		}
		return false;
	}

	@Override
	public Object getCellEditorValue()
	{
		return field.save();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		field.load((LanguageBundle) value);
		return field;
	}
}
