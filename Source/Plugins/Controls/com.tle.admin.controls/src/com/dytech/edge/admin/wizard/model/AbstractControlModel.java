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

package com.dytech.edge.admin.wizard.model;

import java.util.Collections;
import java.util.List;

import com.dytech.edge.admin.wizard.WizardHelper;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.beans.entity.LanguageBundle;

public abstract class AbstractControlModel<T extends WizardControl> extends Control
{
	private T control;

	public AbstractControlModel(ControlDefinition definition)
	{
		super(definition);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.control = (T) wrappedObject;
	}

	public T getControl()
	{
		return control;
	}

	@Override
	public List<String> getTargets()
	{
		return Collections.unmodifiableList(WizardHelper.convertTargetNodes(control.getTargetnodes()));
	}

	@Override
	public List<?> getChildObjects()
	{
		return null;
	}

	@Override
	public String getControlClass()
	{
		return control.getClassType();
	}

	@Override
	public String getScript()
	{
		return control.getScript();
	}

	@Override
	public LanguageBundle getTitle()
	{
		return control.getTitle();
	}

	@Override
	public boolean isPowerSearchInclude()
	{
		return control.isInclude();
	}

	@Override
	public String getCustomName()
	{
		return control.getCustomName();
	}

	@Override
	public void setCustomName(String customName)
	{
		control.setCustomName(customName);

	}

	@Override
	public void setPowerSearchInclude(boolean b)
	{
		control.setInclude(b);
	}

	@Override
	public void setScript(String script)
	{
		control.setScript(script);
	}

	@Override
	public Object save()
	{
		if( allowsChildren() )
		{
			@SuppressWarnings("unchecked")
			List<Object> childObjects = (List<Object>) getChildObjects();

			childObjects.clear();
			for( Control c : getChildren() )
			{
				childObjects.add(c.save());
			}
		}

		return control;
	}

	@Override
	public boolean isRemoveable()
	{
		return control.isRemoveable();
	}

	@Override
	public boolean isScriptable()
	{
		return control.isScriptable();
	}
}
