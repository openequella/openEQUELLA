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

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.dytech.edge.admin.wizard.editor.Editor;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.controls.repository.ControlRepository;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.applet.client.ClientService;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LangUtils;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class Control
{
	private final ControlDefinition definition;
	private ControlRepository controlRepository;
	private Object wrappedObject;
	private Control parent;
	private String errorMessage;
	private final List<Control> children = new ArrayList<Control>();
	private Component editor;

	/**
	 * Constructs a new Control.
	 */
	public Control(ControlDefinition definition)
	{
		this.definition = definition;
	}

	public Control(ControlDefinition definition, Editor editor)
	{
		this.definition = definition;
		this.editor = editor;
	}

	public List<Control> getChildren()
	{
		return children;
	}

	public ControlDefinition getDefinition()
	{
		return definition;
	}

	public Control getParent()
	{
		return parent;
	}

	public void setParent(Control parent)
	{
		this.parent = parent;
	}

	public Object getWrappedObject()
	{
		return wrappedObject;
	}

	public void setWrappedObject(Object wrappedObject)
	{
		this.wrappedObject = wrappedObject;
	}

	public Control getNextSibling()
	{
		Control sibling = null;
		if( parent != null )
		{
			int index = parent.getChildren().indexOf(this);
			int lastIndex = parent.getChildren().size() - 1;

			if( index < lastIndex )
			{
				sibling = parent.getChildren().get(index + 1);
			}
		}
		return sibling;
	}

	public Control getPreviousSibling()
	{
		Control sibling = null;
		if( parent != null )
		{
			int index = parent.getChildren().indexOf(this);
			if( index > 0 )
			{
				sibling = parent.getChildren().get(index - 1);
			}
		}
		return sibling;
	}

	public String doValidation(ClientService clientService)
	{
		return null;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage)
	{
		this.errorMessage = errorMessage;
	}

	public String getTargetBase()
	{
		if( parent != null )
		{
			return parent.getTargetBase();
		}
		return "";
	}

	public abstract String getControlClass();

	public abstract List<? extends Object> getChildObjects();

	public abstract String getScript();

	public abstract void setScript(String script);

	public abstract LanguageBundle getTitle();

	public void setTitle(LanguageBundle title)
	{
		// do nothing
	}

	public abstract boolean isPowerSearchInclude();

	public boolean allowsChildren()
	{
		return false;
	}

	public boolean isScripted()
	{
		return !Check.isEmpty(getScript());
	}

	public abstract void setCustomName(String string);

	public List<String> getTargets()
	{
		return Collections.emptyList();
	}

	public abstract void setPowerSearchInclude(boolean b);

	public abstract String getCustomName();

	public abstract boolean isRemoveable();

	public abstract boolean isScriptable();

	@Override
	public String toString()
	{
		String customName = getCustomName();
		if( customName != null && customName.length() > 0 )
		{
			return customName;
		}

		String t = LangUtils.getString(getTitle(), CurrentLocale.getLocale(), null);
		if( !Check.isEmpty(t) )
		{
			return t;
		}

		else if( definition != null )
		{
			return definition.getName();
		}
		else
		{
			return super.toString();
		}
	}

	public abstract Object save();

	public List<String> getContexts()
	{
		return Collections.emptyList();
	}

	public ControlRepository getControlRepository()
	{
		return controlRepository;
	}

	public void setControlRepository(ControlRepository controlRepository)
	{
		this.controlRepository = controlRepository;
	}

	public synchronized Component getEditor()
	{
		return editor;
	}

	public synchronized void setEditor(Component editor)
	{
		this.editor = editor;
	}
}
