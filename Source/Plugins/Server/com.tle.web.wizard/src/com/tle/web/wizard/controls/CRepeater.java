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
import java.util.Iterator;
import java.util.List;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.Repeater;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.tle.common.i18n.LangUtils;
import com.tle.core.wizard.WizardPageException;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.core.wizard.controls.WizardPage;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.result.util.KeyLabel;

/**
 * @author jmaginnis
 */
public class CRepeater extends GroupsCtrl
{
	private static final long serialVersionUID = 1L;

	protected int min;
	protected int max;
	protected String noun;

	public CRepeater(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
		Repeater repeater = (Repeater) controlBean;
		min = repeater.getMin();
		max = repeater.getMax();

		clearGroups();
		for( int i = 0; i < min; i++ )
		{
			addNewSet(i);
		}
	}

	@Override
	public boolean isEmpty()
	{
		return getGroupSize() < min;
	}

	@Override
	public void doEvaluate()
	{
		defaultEvaluate();
		String target = getFirstTarget().getTarget();
		for( Iterator<ControlGroup> iter = getGroups().iterator(); iter.hasNext(); )
		{
			ControlGroup group = iter.next();
			wizardPage.pushPathOverride(this, target, group.getIndex());
			evaluateGroup(group);
			wizardPage.popPathOverride(this);
		}

		noun = evalString(((Repeater) controlBean).getNoun()).trim();
		if( noun.length() > 0 )
		{
			noun = " " + noun; //$NON-NLS-1$
		}
	}

	public void evaluateGroup(ControlGroup group)
	{
		for( HTMLControl control : group.getControls() )
		{
			control.evaluate();
		}
	}

	public ControlGroup addNewSet(int index)
	{
		List<HTMLControl> controls = new ArrayList<HTMLControl>();
		try
		{
			List<WizardControl> wizControls = ((Repeater) controlBean).getControls();
			wizardPage.createCtrls(wizControls, getNestingLevel() + 1, controls);

			for( HTMLControl control : controls )
			{
				// Hax for path overrides
				control.setParent(this);
			}
		}
		catch( WizardPageException e )
		{
			throw Throwables.propagate(e);
		}

		final ControlGroup group = new ControlGroup(controls, index);
		addGroup(group);
		resetGroup(group);

		final WizardGroupListener listener = getListener();
		if( listener != null )
		{
			listener.addNewGroup(group);
		}
		return group;
	}

	@Override
	public void loadFromDocument(PropBagEx docPropBag)
	{
		String target = getFirstTarget().getTarget();

		int i = 0;
		for( PropBagEx targBag : docPropBag.iterator(target) )
		{
			wizardPage.pushPathOverride(this, target, i);
			if( getGroupSize() <= i )
			{
				evaluateGroup(addNewSet(i));
			}

			ControlGroup group = getGroups().get(i);
			loadGroup(group, targBag, false);
			wizardPage.popPathOverride(this);
			i++;
		}
	}

	@Override
	public void validate()
	{
		if( getGroupSize() < min )
		{
			String key = "wizard.controls.repeater.entermin"; //$NON-NLS-1$
			if( min == 1 )
			{
				key += "one"; //$NON-NLS-1$
			}
			setInvalid(true, new KeyLabel(key, min));
		}
		super.validate();
	}

	@Override
	public void saveToDocument(PropBagEx docPropBag) throws Exception
	{
		clearTargets(docPropBag);
		String target = getFirstTarget().getTarget();
		for( ControlGroup group : getGroups() )
		{
			PropBagEx targBag = docPropBag.newSubtree(target);
			saveGroup(group, targBag, false);
		}
	}

	@Override
	public void afterSaveValidate()
	{
		// This bit same as AbstractHtmlControl, but we want to bypass
		// GroupsCtrl implementation
		String script = controlBean.getAfterSaveScript();
		if( script != null )
		{
			execScript(script);
		}

		String target = getFirstTarget().getTarget();
		int i = 0;
		for( Iterator<ControlGroup> iter = getGroups().iterator(); iter.hasNext(); i++ )
		{
			ControlGroup group = iter.next();
			wizardPage.pushPathOverride(this, target, i);
			afterSaveValidateGroup(group);
			wizardPage.popPathOverride(this);
		}
	}

	public String getNoun()
	{
		return noun;
	}

	public int getMax()
	{
		return max;
	}

	public int getMin()
	{
		return min;
	}

	public void removeGroup(SectionInfo info, int i)
	{
		removeGroup(i);
		final WizardGroupListener listener = getListener();
		if( listener != null )
		{
			listener.removeFromGroup(info, i);
		}
		updateIndexes();
	}

	public void swapGroups(int groupOneIndex, int groupTwoIndex)
	{
		ImmutableList<ControlGroup> groups = getGroups();
		ControlGroup controlOne = groups.get(groupOneIndex);
		ControlGroup controlTwo = groups.get(groupTwoIndex);
		removeGroup(groupOneIndex);
		addGroup(groupOneIndex, controlTwo);
		removeGroup(groupTwoIndex);
		addGroup(groupTwoIndex, controlOne);
		updateIndexes();
		validate();
	}

	private void updateIndexes()
	{
		ImmutableList<ControlGroup> groups = getGroups();
		for( int i = 0; i < groups.size(); i++ )
		{
			ControlGroup group = groups.get(i);
			group.setIndex(i);
		}
	}

	public void addAndEvaluate()
	{
		int index = getGroupSize();
		// why (was) it index +1?
		wizardPage.pushPathOverride(this, getFirstTarget().getTarget(), index);
		evaluateGroup(addNewSet(index));
		wizardPage.popPathOverride(this);
	}
}
