package com.tle.admin.helper;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import com.dytech.gui.ComponentHelper;

@SuppressWarnings("nls")
public class ShowHideComponent<T extends JComponent> extends JComponent
{
	protected final T comp;
	protected final AbstractButton button;

	public ShowHideComponent(final AbstractButton button, T comp)
	{
		this.button = button;
		this.comp = comp;

		button.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				updateState();
			}
		});

		setLayout(new MigLayout("insets 0, wrap, hidemode 3", "[grow]", "[][grow]"));
		add(button);
		add(comp, "gap indent, grow");

		updateState();
	}

	public AbstractButton getButton()
	{
		return button;
	}

	public T getShowHideComponent()
	{
		return comp;
	}

	public void setSelected(boolean b)
	{
		if( button.isSelected() != b )
		{
			button.setSelected(b);
			updateState();
		}
	}

	private void updateState()
	{
		comp.setVisible(button.isEnabled() && button.isSelected());
	}

	public boolean isSelected()
	{
		return button.isSelected();
	}

	public void addToGroup(final ButtonGroup group)
	{
		group.add(button);
	}

	@Override
	public void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		button.setEnabled(enabled);
		updateState();
	}

	public static void main(String[] args)
	{
		JLabel l1 = new JLabel(
			"<html>This is a really long label with lots of stuff that should flow over multiple lines; who knows how many, but it could be heaps.");

		JCheckBox cb = new JCheckBox("Check box!");
		JLabel l2 = new JLabel(
			"<html>This is a really long label with lots of stuff that should flow over multiple lines; who knows how many, but it could be heaps.");

		ShowHideComponent<JLabel> shc = new ShowHideComponent<JLabel>(cb, l2);

		JPanel p = new JPanel(new MigLayout("wrap"));
		p.add(l1);
		p.add(shc);

		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(400, 400);
		frame.setContentPane(p);
		ComponentHelper.centreOnScreen(frame);
		frame.setVisible(true);
	}
}
