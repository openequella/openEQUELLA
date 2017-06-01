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

package com.tle.core.taxonomy.schema;

import com.dytech.edge.wizard.beans.control.Multi;

public class PickList extends Multi
{
	private static final long serialVersionUID = 1;
	public static final String CLASS = "picklist"; //$NON-NLS-1$

	private String taxonomy;
	private boolean multiple;

	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public String getTaxonomy()
	{
		return taxonomy;
	}

	public void setTaxonomy(String taxonomy)
	{
		this.taxonomy = taxonomy;
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

}
