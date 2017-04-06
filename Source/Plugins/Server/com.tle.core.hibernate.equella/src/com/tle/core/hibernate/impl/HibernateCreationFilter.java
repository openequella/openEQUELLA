package com.tle.core.hibernate.impl;

import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Index;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.UniqueKey;

public interface HibernateCreationFilter
{
	boolean includeTable(Table table);

	boolean includeUniqueKey(Table table, UniqueKey uk);

	boolean includeIndex(Table table, Index index);

	boolean includeForeignKey(Table table, ForeignKey fk);

	boolean includeGenerator(PersistentIdentifierGenerator pig);

	boolean includeObject(AuxiliaryDatabaseObject object);

}
