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

import java.util.List;

import com.dytech.edge.wizard.beans.FixedMetadata;
import com.dytech.edge.wizard.beans.Metadata;
import com.tle.admin.controls.repository.ControlDefinition;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class MetadataModel extends BasicAbstractControl
{
	private FixedMetadata fixed;

	/**
	 * Constructs a new MetadataModel
	 */
	public MetadataModel(ControlDefinition definition)
	{
		super(definition);
	}

	@Override
	public void setWrappedObject(Object wrappedObject)
	{
		super.setWrappedObject(wrappedObject);
		this.fixed = (FixedMetadata) wrappedObject;
	}

	/**
	 * @return Returns the metadata.
	 */
	public List<Metadata> getMetadata()
	{
		return fixed.getData();
	}

	@Override
	public String getControlClass()
	{
		return "metadata";
	}

	@Override
	public Object save()
	{
		return fixed;
	}

	@Override
	public void setScript(String script)
	{
		fixed.setScript(script);
	}

	@Override
	public String getScript()
	{
		return fixed.getScript();
	}

}
