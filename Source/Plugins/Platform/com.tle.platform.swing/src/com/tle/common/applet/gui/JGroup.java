package com.tle.common.applet.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @deprecated Use GroupBox instead.
 */
@Deprecated
public class JGroup extends JPanel implements ChangeListener, ActionListener, ItemSelectable
{
	private static final long serialVersionUID = 1L;
	protected JPanel inner;
	protected JCheckBox button;

	public JGroup(String title)
	{
		this(title, false);
	}

	public JGroup(String title, boolean selected)
	{
		inner = new JPanel();

		JPanel outer = new JPanel(new GridLayout(1, 1));
		outer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
			BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		outer.add(inner);

		button = new JCheckBox(title, selected);
		button.addActionListener(this);
		button.addChangeListener(this);

		super.setLayout(new BorderLayout());
		add(button, BorderLayout.NORTH);
		add(outer, BorderLayout.CENTER);
	}

	public void setInnerLayout(LayoutManager layout)
	{
		inner.setLayout(layout);
	}

	public Component addInner(Component comp)
	{
		inner.add(comp);
		return comp;
	}

	public void addInner(Component comp, Object constraints)
	{
		inner.add(comp, constraints);
	}

	public boolean isSelected()
	{
		return button.isSelected();
	}

	public void setSelected(boolean b)
	{
		enableInner(b);
		button.setSelected(b);
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		enableInner(enabled && isSelected());
		button.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	private void enableInner(boolean enabled)
	{
		Component[] all = inner.getComponents();
		for( Component element : all )
		{
			element.setEnabled(enabled);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == button )
		{
			setSelected(button.isSelected());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if( e.getSource() == button )
		{
			setSelected(button.isSelected());
		}
	}

	public void addActionListener(ActionListener listener)
	{
		button.addActionListener(listener);
	}

	@Override
	public void addItemListener(ItemListener l)
	{
		button.addItemListener(l);
	}

	@Override
	public void removeItemListener(ItemListener l)
	{
		button.removeItemListener(l);
	}

	@Override
	public Object[] getSelectedObjects()
	{
		return button.getSelectedObjects();
	}
}
