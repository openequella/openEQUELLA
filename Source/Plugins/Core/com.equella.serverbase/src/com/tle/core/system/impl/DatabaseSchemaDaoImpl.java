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

package com.tle.core.system.impl;

import java.util.Collection;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.DatabaseSchema;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.SystemDatabase;
import com.tle.core.system.dao.DatabaseSchemaDao;

@Singleton
@SystemDatabase
@Bind(DatabaseSchemaDao.class)
public class DatabaseSchemaDaoImpl extends AbstractSystemDaoImpl<DatabaseSchema, Long> implements DatabaseSchemaDao
{
	public DatabaseSchemaDaoImpl()
	{
		super(DatabaseSchema.class);
	}

	@Override
	@Transactional
	public Collection<DatabaseSchema> enumerate()
	{
		return findAllByCriteria();
	}

	@Override
	@Transactional
	public DatabaseSchema setOnline(long schemaId, boolean online)
	{
		DatabaseSchema schema = findById(schemaId);
		schema.setOnline(online);
		return schema;
	}

	@Override
	@Transactional
	public boolean deleteSchema(long schemaId)
	{
		DatabaseSchema schema = findById(schemaId);
		if( schema != null )
		{
			delete(schema);
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public DatabaseSchema get(long schemaId)
	{
		return findById(schemaId);
	}

	@Override
	@Transactional
	public long add(DatabaseSchema ds)
	{
		return save(ds);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void edit(DatabaseSchema ds)
	{
		merge(ds);
	}
}
