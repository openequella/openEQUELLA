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

package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.IfModel;

public class ContainsComparison implements Comparison
{
	protected String xpath;
	protected String value;

	public ContainsComparison(String xpath, String value)
	{
		this.xpath = xpath;
		this.value = value;
	}

	public String getXpath()
	{
		return xpath;
	}

	public void setXpath(String xpath)
	{
		this.xpath = xpath;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String toScript()
	{
		return "xml.contains('" + xpath + "', '" + IfModel.encode(value) + "')";
	}

	@Override
	public String toEasyRead()
	{
		// We want to make contains look like equals, so we don't want
		// the following anymore.
		// return xpath + " <b>contains</b> '" + value + "'";

		return xpath + " <b>=</b> '" + value + "'";
	}
}
