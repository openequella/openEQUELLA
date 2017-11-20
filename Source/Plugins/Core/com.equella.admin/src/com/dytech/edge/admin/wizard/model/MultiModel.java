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

import java.util.Arrays;
import java.util.List;

import com.dytech.edge.admin.wizard.Validation;
import com.dytech.edge.wizard.beans.control.Multi;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.common.applet.client.ClientService;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class MultiModel extends AbstractControlModel<Multi>
{
	private Multi multi;

	/**
	 * Constructs a new MultiModel.
	 */
	public MultiModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.multi = (Multi) wrappedObject;
	}

	@Override
	public List<?> getChildObjects()
	{
		return multi.getControls();
	}

	@Override
	public boolean allowsChildren()
	{
		return true;
	}

	@Override
	public String getTargetBase()
	{
		String base = super.getTargetBase();
		if( !multi.getTargetnodes().isEmpty() )
		{
			base += multi.getTargetnodes().get(0).getTarget();
		}
		return base;
	}

	@Override
	public List<String> getContexts()
	{
		return Arrays.asList("multi");
	}

	@Override
	public String doValidation(ClientService clientService)
	{
		String error = Validation.hasTarget(getControl());

		if( error == null )
		{
			error = Validation.hasChildren(this);
		}

		return error;
	}
}
