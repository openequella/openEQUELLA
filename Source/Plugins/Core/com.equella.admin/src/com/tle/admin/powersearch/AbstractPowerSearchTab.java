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

package com.tle.admin.powersearch;

import com.dytech.gui.Changeable;
import com.tle.admin.baseentity.BaseEntityTab;
import com.tle.admin.schema.SchemaModel;
import com.tle.beans.entity.PowerSearch;

public abstract class AbstractPowerSearchTab extends BaseEntityTab<PowerSearch>
{
	protected Changeable parentChangeDetector;
	protected SchemaModel schema;

	public void setSchemaModel(SchemaModel schema)
	{
		this.schema = schema;
	}

	public void setParentChangeDetector(Changeable parentChangeDetector)
	{
		this.parentChangeDetector = parentChangeDetector;
	}
}
