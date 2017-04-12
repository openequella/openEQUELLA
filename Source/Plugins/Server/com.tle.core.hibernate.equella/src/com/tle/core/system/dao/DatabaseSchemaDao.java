package com.tle.core.system.dao;

import java.util.Collection;

import com.tle.beans.DatabaseSchema;
import com.tle.core.hibernate.dao.GenericDao;

public interface DatabaseSchemaDao extends GenericDao<DatabaseSchema, Long>
{
	// Nothing yet

	Collection<DatabaseSchema> enumerate();

	DatabaseSchema setOnline(long schemaId, boolean online);

	DatabaseSchema get(long schemaId);

	long add(DatabaseSchema ds);

	void edit(DatabaseSchema ds);

	boolean deleteSchema(long schemaId);
}
