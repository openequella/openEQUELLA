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

package com.tle.core.hibernate.impl;

import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

public class AllDataHibernateMigrationFilter implements HibernateCreationFilter
{
	private boolean includeGenerators = true;

	@Override
	public boolean includeForeignKey(Table table, ForeignKey fk)
	{
		return true;
	}

	@Override
	public boolean includeGenerator(PersistentIdentifierGenerator pig)
	{
		return includeGenerators;
	}

	@Override
	public boolean includeIndex(Table table, Index index)
	{
		return true;
	}

	@Override
	public boolean includeObject(AuxiliaryDatabaseObject object)
	{
		return true;
	}

	@Override
	public boolean includeTable(Table table)
	{
		return true;
	}

	@Override
	public boolean includeUniqueKey(Table table, UniqueKey uk)
	{
		return true;
	}

	public boolean isIncludeGenerators()
	{
		return includeGenerators;
	}

	public void setIncludeGenerators(boolean includeGenerators)
	{
		this.includeGenerators = includeGenerators;
	}

}
