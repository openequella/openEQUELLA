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
