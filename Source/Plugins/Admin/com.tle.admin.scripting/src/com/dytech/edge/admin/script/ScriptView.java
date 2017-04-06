package com.dytech.edge.admin.script;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ScriptView extends JList implements ListSelectionListener
{
	private static final long serialVersionUID = 1L;
	protected ScriptModel model;

	public ScriptView(ScriptModel model)
	{
		super(model);
		this.model = model;
		setCellRenderer(new MyCellRenderer());
		addListSelectionListener(this);
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if( !isSelectionEmpty() )
		{
			Row r = (Row) getSelectedValue();
			model.rowSelected(r, getSelectedIndex());
		}
	}

	protected class MyCellRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 1L;
		private static final String DOT_NAME_1 = "/icons/script_dot.gif";
		private static final String DOT_NAME_2 = "/icons/script_dot2.gif";

		private final ImageIcon dot1;
		private final ImageIcon dot2;

		public MyCellRenderer()
		{
			dot1 = new ImageIcon(MyCellRenderer.class.getResource(DOT_NAME_1));
			dot2 = new ImageIcon(MyCellRenderer.class.getResource(DOT_NAME_2));
		}

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
			boolean cellHasFocus)
		{
			if( isSelected )
			{
				setBackground(new Color(151, 201, 155));
				setIcon(dot2);
			}
			else
			{
				setBackground(Color.white);
				setIcon(dot1);
			}

			setFont(new Font("Monospaced", Font.PLAIN, 12));
			setText(value.toString());

			return this;
		}
	}
}
