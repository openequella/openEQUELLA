/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dytech.installer;

import javax.swing.AbstractButton;

public class Item
{
	protected String name;
	protected String value;
	protected boolean selected;
	protected AbstractButton button;

	public Item()
	{
		// Nothing to do here
	}

	public Item(String name, String value, boolean selected)
	{
		this.name = name;
		this.value = value;
		this.selected = selected;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setButton(AbstractButton button)
	{
		this.button = button;
	}

	public AbstractButton getButton()
	{
		return button;
	}

	@Override
	public String toString()
	{
		return name;
	}
}
