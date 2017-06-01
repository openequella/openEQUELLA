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

package com.dytech.edge.admin.script.model;

import com.dytech.edge.admin.script.Row;

public class Node implements Row
{
	protected Object parent;
	protected String text;

	public void setParent(Object parent)
	{
		this.parent = parent;
	}

	public Object getParent()
	{
		return parent;
	}

	public void setText(String text)
	{
		this.text = "<html>" + text;
	}

	public void appendText(String text)
	{
		this.text += text;
	}

	@Override
	public String toString()
	{
		return text;
	}
}
