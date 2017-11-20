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

public class EditBox extends WizardControl
{
	private static final long serialVersionUID = 1;
	public static final String CLASS = "editbox";

	private boolean links;
	private boolean number;
	private boolean forceUnique;
	private boolean checkDuplication;
	private boolean allowMultiLang;

	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public boolean isAllowLinks()
	{
		return links;
	}

	public void setAllowLinks(boolean links)
	{
		this.links = links;
	}

	public boolean isForceUnique()
	{
		return forceUnique;
	}

	public void setForceUnique(boolean forceUnique)
	{
		this.forceUnique = forceUnique;
	}

	public boolean isNumber()
	{
		return number;
	}

	public void setNumber(boolean number)
	{
		this.number = number;
	}

	public boolean isAllowMultiLang()
	{
		return allowMultiLang;
	}

	public void setAllowMultiLang(boolean allowMultiLang)
	{
		this.allowMultiLang = allowMultiLang;
	}

	public boolean isCheckDuplication()
	{
		return checkDuplication;
	}

	public void setCheckDuplication(boolean checkDuplication)
	{
		this.checkDuplication = checkDuplication;
	}
}
