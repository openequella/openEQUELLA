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

package com.dytech.edge.wizard.beans.control;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.wizard.TargetNode;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.entity.LanguageBundle;

public abstract class WizardControl implements Serializable
{
	private static final long serialVersionUID = 1;

	private boolean mandatory;
	private boolean reload;
	private boolean include;
	private int size1;
	private int size2;
	private String customName;
	private LanguageBundle title;
	private LanguageBundle description;
	private String script;
	private List<TargetNode> targetnodes = new ArrayList<TargetNode>();
	private final List<WizardControlItem> items = new ArrayList<WizardControlItem>();
	private String validateScript;
	private String afterSaveScript;
	private LanguageBundle powerSearchFriendlyName;

	// Can't remove this field as we're xstreamed
	@XStreamOmitField
	@SuppressWarnings("unused")
	private WizardControl parent;

	public WizardControl()
	{
		size1 = 0;
		size2 = 0;
		include = true;
	}

	public abstract String getClassType();

	public String getCustomName()
	{
		return customName;
	}

	public void setCustomName(String customName)
	{
		this.customName = customName;
	}

	public LanguageBundle getDescription()
	{
		return description;
	}

	public void setDescription(LanguageBundle description)
	{
		this.description = description;
	}

	public boolean isMandatory()
	{
		return mandatory;
	}

	public void setMandatory(boolean mandatory)
	{
		this.mandatory = mandatory;
	}

	public boolean isReload()
	{
		return reload;
	}

	public void setReload(boolean reload)
	{
		this.reload = reload;
	}

	public int getSize1()
	{
		return size1;
	}

	public void setSize1(int size1)
	{
		this.size1 = size1;
	}

	public int getSize2()
	{
		return size2;
	}

	public void setSize2(int size2)
	{
		this.size2 = size2;
	}

	public LanguageBundle getTitle()
	{
		return title;
	}

	public void setTitle(LanguageBundle title)
	{
		this.title = title;
	}

	public List<WizardControlItem> getItems()
	{
		return items;
	}

	public WizardControlItem getItem(int index)
	{
		if( items.size() <= index )
		{
			return null;
		}
		else
		{
			return items.get(index);
		}
	}

	public String getItemValue(int index)
	{
		WizardControlItem item = getItem(index);
		if( item == null )
		{
			return null;
		}
		return item.getValue();
	}

	public List<TargetNode> getTargetnodes()
	{
		return targetnodes;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public boolean isInclude()
	{
		return include;
	}

	public void setInclude(boolean include)
	{
		this.include = include;
	}

	public String getValidateScript()
	{
		return validateScript;
	}

	public void setValidateScript(String validateScript)
	{
		this.validateScript = validateScript;
	}

	public String getAfterSaveScript()
	{
		return afterSaveScript;
	}

	public void setAfterSaveScript(String afterSaveScript)
	{
		this.afterSaveScript = afterSaveScript;
	}

	public void setTargetnodes(List<TargetNode> targetnodes)
	{
		this.targetnodes = targetnodes;
	}

	protected <T extends WizardControl> void cloneTo(T control)
	{
		cloneFields(WizardControl.class, control);
	}

	protected void cloneFields(Class<? extends WizardControl> clazz, WizardControl control)
	{
		Field[] declaredFields = clazz.getDeclaredFields();
		AccessibleObject.setAccessible(declaredFields, true);
		for( Field field : declaredFields )
		{
			try
			{
				if( (field.getModifiers() & (Modifier.STATIC | Modifier.FINAL)) == 0 )
				{
					field.set(control, field.get(this));
				}
			}
			catch( Exception e )
			{
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isRemoveable()
	{
		return true;
	}

	public boolean isScriptable()
	{
		return true;
	}

	public LanguageBundle getPowerSearchFriendlyName()
	{
		return powerSearchFriendlyName;
	}

	public void setPowerSearchFriendlyName(LanguageBundle powerSearchFriendlyName)
	{
		this.powerSearchFriendlyName = powerSearchFriendlyName;
	}
}
