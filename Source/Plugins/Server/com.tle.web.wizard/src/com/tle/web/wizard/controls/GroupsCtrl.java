/*
 * Copyright 2017 Apereo
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

package com.tle.web.wizard.controls;

import java.util.ArrayList;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.tle.beans.entity.LanguageBundle;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;

public class GroupsCtrl extends OptionCtrl
{
	private static final long serialVersionUID = 1L;

	private WizardGroupListener listener;
	private final List<ControlGroup> groups = new ArrayList<ControlGroup>();
	private final transient Object groupsLock = new Object();

	public GroupsCtrl(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	protected void clearGroups()
	{
		synchronized( groupsLock )
		{
			groups.clear();
		}
	}

	protected void addGroup(ControlGroup group)
	{
		synchronized( groupsLock )
		{
			groups.add(group);
		}
	}

	protected void addGroup(int index, ControlGroup group)
	{
		synchronized( groupsLock )
		{
			groups.add(index, group);
		}
	}

	protected void removeGroup(int index)
	{
		synchronized( groupsLock )
		{
			groups.remove(index);
		}
	}

	protected int getGroupSize()
	{
		synchronized( groupsLock )
		{
			return groups.size();
		}
	}

	@Override
	public void doEvaluate()
	{
		super.doEvaluate();

		for( ControlGroup group : getGroups() )
		{
			for( HTMLControl control : group.getControls() )
			{
				control.evaluate();
			}
		}
	}

	@Override
	public void resetToDefaults()
	{
		for( ControlGroup group : getGroups() )
		{
			resetGroup(group);
		}
	}

	@Override
	public void setDontShowEmpty(boolean b)
	{
		super.setDontShowEmpty(b);
		for( ControlGroup group : getGroups() )
		{
			for( HTMLControl c : group.getControls() )
			{
				c.setDontShowEmpty(b);
			}
		}
	}

	@Override
	public void validate()
	{
		for( ControlGroup group : getGroups() )
		{
			for( HTMLControl c : group.getControls() )
			{
				validate(c);
			}
		}
	}

	@Override
	public void clearInvalid()
	{
		super.clearInvalid();
		for( ControlGroup group : getGroups() )
		{
			for( HTMLControl c : group.getControls() )
			{
				c.clearInvalid();
			}
		}
	}

	protected void validate(HTMLControl control)
	{
		control.validate();
		if( control.isViewable() && (control.isInvalid() || (control.isEmpty() && control.isMandatory())) )
		{
			setInvalid(true, null);
			return;
		}
	}

	@Override
	public void setHidden(boolean hidden)
	{
		super.setHidden(hidden);
		for( ControlGroup group : getGroups() )
		{
			for( HTMLControl c : group.getControls() )
			{
				c.setHidden(hidden);
			}
		}
	}

	public ImmutableList<ControlGroup> getGroups()
	{
		synchronized( groupsLock )
		{
			return ImmutableList.copyOf(groups);
		}
	}

	protected void loadGroup(ControlGroup group, PropBagEx itemxml, boolean dontshow)
	{
		for( HTMLControl ctrl : group.getControls() )
		{
			if( ctrl.isViewable() )
			{
				ctrl.loadFromDocument(itemxml);
			}
		}
	}

	protected void resetGroup(ControlGroup group)
	{
		for( HTMLControl ctrl : group.getControls() )
		{
			ctrl.resetToDefaults();
		}
	}

	protected void saveGroup(ControlGroup group, PropBagEx itemxml, boolean clear) throws Exception
	{
		for( HTMLControl ctrl : group.getControls() )
		{
			// only process the values if not disabled
			if( clear || !ctrl.isVisible() )
			{
				ctrl.clearTargets(itemxml);
			}
			else if( !ctrl.isHidden() )
			{
				ctrl.saveToDocument(itemxml);
			}
		}
	}

	@Override
	public void afterSaveValidate()
	{
		super.afterSaveValidate();
		for( ControlGroup group : getGroups() )
		{
			afterSaveValidateGroup(group);
		}
	}

	protected void afterSaveValidateGroup(ControlGroup group)
	{
		for( HTMLControl ctrl : group.getControls() )
		{
			try
			{
				ctrl.afterSaveValidate();
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
		}
	}

	public void setListener(WizardGroupListener listener)
	{
		this.listener = listener;
	}

	protected WizardGroupListener getListener()
	{
		return listener;
	}

	public static class ControlGroup
	{
		private final List<HTMLControl> controls;
		private int index;

		public ControlGroup(List<HTMLControl> controls, int index)
		{
			super();
			this.controls = controls;
			this.index = index;
		}

		public List<HTMLControl> getControls()
		{
			return controls;
		}

		public int getIndex()
		{
			return index;
		}

		public void setIndex(int index)
		{
			this.index = index;
		}

		public boolean contains(HTMLControl control)
		{
			return controls.contains(control);
		}
	}
}
