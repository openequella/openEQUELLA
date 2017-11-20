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

package com.tle.core.hibernate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Table;
import org.hibernate.type.BasicType;

public class ExtendedAnnotationConfiguration extends Configuration
{
	private static final long serialVersionUID = 1L;

	public ExtendedAnnotationConfiguration(ExtendedDialect dialect)
	{
		Iterable<? extends BasicType> types = dialect.getExtraTypeOverrides();
		for( BasicType basicType : types )
		{
			registerTypeOverride(basicType);
		}
	}

	public Map<String, Table> getTableMap()
	{
		return tables;
	}

	public List<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjects()
	{
		return auxiliaryDatabaseObjects;
	}

	public java.util.Collection<PersistentIdentifierGenerator> getGenerators(Dialect dialect, String defaultCatalog,
		String defaultSchema)
	{
		TreeMap<Object, PersistentIdentifierGenerator> generators = new TreeMap<Object, PersistentIdentifierGenerator>();

		Iterator<PersistentClass> iter = classes.values().iterator();
		while( iter.hasNext() )
		{
			PersistentClass pc = iter.next();

			if( !pc.isInherited() )
			{
				IdentifierGenerator ig = pc.getIdentifier().createIdentifierGenerator(getIdentifierGeneratorFactory(),
					dialect, defaultCatalog, defaultSchema, (RootClass) pc);

				if( ig instanceof PersistentIdentifierGenerator )
				{
					PersistentIdentifierGenerator pig = (PersistentIdentifierGenerator) ig;
					generators.put(pig.generatorKey(), pig);
				}

			}
		}

		Iterator<Collection> coliter = collections.values().iterator();
		while( coliter.hasNext() )
		{
			Collection collection = coliter.next();

			if( collection.isIdentified() )
			{

				IdentifierGenerator ig = ((IdentifierCollection) collection).getIdentifier().createIdentifierGenerator(
					getIdentifierGeneratorFactory(), dialect, defaultCatalog, defaultSchema, null);

				if( ig instanceof PersistentIdentifierGenerator )
				{
					PersistentIdentifierGenerator pig = (PersistentIdentifierGenerator) ig;
					generators.put(pig.generatorKey(), pig);
				}

			}
		}

		return generators.values();

	}
}
