/*
 * Copyright 2019 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.admin.gui.common;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.text.JTextComponent;

import com.dytech.gui.ChangeDetector;
import com.dytech.gui.Changeable;
import com.dytech.gui.filter.FilteredShuffleBox;

@Deprecated
public class JChangeDetectorPanel extends JPanel implements Changeable
{
	private static final long serialVersionUID = 1L;
	private final ChangeDetector changeDetector;

	public JChangeDetectorPanel()
	{
		changeDetector = new ChangeDetector();
	}

	@Override
	protected void addImpl(Component comp, Object constraints, int index)
	{
		if( !watch(comp) && comp instanceof Container )
		{
			recurse((Container) comp);
		}

		if( comp instanceof JIgnoreChangeComponent )
		{
			comp = ((JIgnoreChangeComponent) comp).getChild();
		}

		super.addImpl(comp, constraints, index);
	}

	public void watch(ListModel listModel)
	{
		changeDetector.watch(listModel);
	}

	public boolean watch(Component comp)
	{
		if( comp instanceof JTextComponent )
		{
			changeDetector.watch((JTextComponent) comp);
		}
		else if( comp instanceof JSpinner )
		{
			changeDetector.watch(((JSpinner) comp).getModel());
		}
		else if( comp instanceof FilteredShuffleBox<?> )
		{
			changeDetector.watch(((FilteredShuffleBox<?>) comp).getRightModel());
		}
		else if( comp instanceof JComboBox )
		{
			changeDetector.watch((JComboBox) comp);
		}
		else if( comp instanceof JCheckBox )
		{
			changeDetector.watch((JCheckBox) comp);
		}
		else if( comp instanceof JRadioButton )
		{
			changeDetector.watch((JRadioButton) comp);
		}
		else if( comp instanceof Changeable )
		{
			changeDetector.watch((Changeable) comp);
		}
		else if( comp instanceof JTable )
		{
			changeDetector.watch(((JTable) comp).getModel());
		}
		else if( comp instanceof JTree )
		{
			changeDetector.watch(((JTree) comp).getModel());
		}
		else if( comp instanceof JList )
		{
			changeDetector.watch(((JList) comp).getModel());
		}
		else
		{
			return false;
		}
		return true;
	}

	protected void recurse(Container container)
	{
		for( Component comp : container.getComponents() )
		{
			if( !watch(comp) && comp instanceof Container && !(comp instanceof JChangeDetectorPanel) )
			{
				recurse((Container) comp);
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		recurseEnabled(this, enabled);
	}

	/**
	 * TODO: This is really dangerous - recusively en/disabling all components
	 * inside every panel will screw up things like JGroup, JShuffleList,
	 * JShuffleBox, and anything else that en/disables stuff depending on other
	 * settings.
	 */
	protected void recurseEnabled(Container container, boolean enable)
	{
		for( Component comp : container.getComponents() )
		{
			comp.setEnabled(enable);
			if( comp instanceof Container && !(comp instanceof JChangeDetectorPanel) )
			{
				recurseEnabled((Container) comp, enable);
			}
		}
	}

	@Override
	public void clearChanges()
	{
		changeDetector.clearChanges();
	}

	@Override
	public boolean hasDetectedChanges()
	{
		return changeDetector.hasDetectedChanges();
	}

	public void setIgnoreChanges(boolean ignore)
	{
		changeDetector.setIgnoreChanges(ignore);
	}

	public void forceChange()
	{
		changeDetector.forceChange(this);
	}

	public ChangeDetector getChangeDetector()
	{
		return changeDetector;
	}

	public static class JIgnoreChangeComponent extends JComponent
	{
		private static final long serialVersionUID = 1L;
		private final JComponent child;

		public JIgnoreChangeComponent(JComponent child)
		{
			this.child = child;
		}

		public JComponent getChild()
		{
			return child;
		}
	}
}
