package com.tle.web.filemanager.applet.gui;

import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import com.tle.common.i18n.CurrentLocale;

/**
 * http://forums.sun.com/thread.jspa?forumID=256&threadID=405848
 */
public abstract class ExpandLayout implements LayoutManager
{
	private JPopupMenu extenderPopup = new JPopupMenu();
	private String popupText = CurrentLocale.isRightToLeft() ? "<<" : ">>";
	private JButton extenderButton = new JButton(new PopupAction(popupText));

	// TODO: handle rtl and when the bar gets too large
	@Override
	public void layoutContainer(Container parent)
	{
		// Position all buttons in the container
		Insets insets = parent.getInsets();
		int x = CurrentLocale.isRightToLeft() ? insets.right : insets.left;
		int y = insets.top;
		int spaceUsed = insets.right + insets.left;
		int parentWidth = parent.getSize().width;

		for( int i = 0; i < parent.getComponentCount(); i++ )
		{
			Component aComponent = parent.getComponent(i);
			aComponent.setSize(aComponent.getPreferredSize());
			int componentWidth = aComponent.getPreferredSize().width;
			if( CurrentLocale.isRightToLeft() )
			{
				x += componentWidth;
				aComponent.setLocation(parentWidth - x, y);

			}
			else
			{
				aComponent.setLocation(x, y);
				x += componentWidth;
			}

			spaceUsed += componentWidth;
		}

		// All the buttons won't fit, add extender button
		// Note: the size of the extender button changes once it is added
		// to the container. Add it here so correct width is used.

		if( spaceUsed > parentWidth )
		{
			parent.add(extenderButton);
			extenderButton.setSize(extenderButton.getPreferredSize());
			spaceUsed += extenderButton.getSize().width;
			if( CurrentLocale.isRightToLeft() )
			{
				extenderButton.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			}
		}

		// Remove buttons that don't fit and add to the popup menu
		while( spaceUsed > parentWidth )
		{
			int last = parent.getComponentCount() - 2;
			AbstractButton aComponent = (AbstractButton) parent.getComponent(last);
			parent.remove(last);
			extenderPopup.insert(aComponent.getAction(), 0);
			if( CurrentLocale.isRightToLeft() )
			{
				extenderButton.setLocation(parent.getComponent(last - 1).getLocation().x - extenderButton.getWidth(),
					aComponent.getLocation().y);
			}
			else
			{
				extenderButton.setLocation(aComponent.getLocation());
			}
			spaceUsed -= aComponent.getSize().width;
		}
	}

	@Override
	public Dimension minimumLayoutSize(Container parent)
	{
		return extenderButton.getMinimumSize();
	}

	@Override
	public Dimension preferredLayoutSize(Container parent)
	{
		// Move all components to the container and remove the extender button
		parent.remove(extenderButton);

		while( extenderPopup.getComponentCount() > 0 )
		{
			AbstractButton aComponent = (AbstractButton) extenderPopup.getComponent(0);
			extenderPopup.remove(aComponent);
			parent.add(createComponent(aComponent.getAction()));
		}

		// Calculate the width of all components in the container
		Dimension d = new Dimension();
		d.width += parent.getInsets().right + parent.getInsets().left;

		for( int i = 0; i < parent.getComponents().length; i++ )
		{
			d.width += parent.getComponent(i).getPreferredSize().width;
			d.height = Math.max(d.height, parent.getComponent(i).getPreferredSize().height);
		}

		d.height += parent.getInsets().top + parent.getInsets().bottom + 5;
		return d;
	}

	@Override
	public void addLayoutComponent(String name, Component comp)
	{
		// Nothing to do here
	}

	@Override
	public void removeLayoutComponent(Component comp)
	{
		// Nothing to do here
	}

	public abstract Component createComponent(Action a);

	protected class PopupAction extends AbstractAction
	{
		public PopupAction(String direction)
		{
			super(direction);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			JComponent component = (JComponent) e.getSource();
			extenderPopup.show(component, 0, component.getHeight());
		}
	}
}
