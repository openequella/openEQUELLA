/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.hibernate.dialect.LowercaseImprovedNamingScheme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Table;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.type.BasicType;

public class ExtendedAnnotationConfiguration extends Configuration {
  private static final long serialVersionUID = 1L;

  public void setNamingStrategy(LowercaseImprovedNamingScheme lowercaseImprovedNamingScheme) {
    // TODO [SpringHib5] need to figure out how to do the equiv in hib5.

  }

  private static class MetadataCapture implements Integrator {
    private Metadata metadata;

    public Metadata getMetadata() {
      return metadata;
    }

    @Override
    public void integrate(
        Metadata metadata,
        SessionFactoryImplementor sessionFactory,
        SessionFactoryServiceRegistry serviceRegistry) {
      this.metadata = metadata;
    }

    @Override
    public void disintegrate(
        SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
      this.metadata = null;
    }
  }

  private static final MetadataCapture METADATA_CAPTURE = new MetadataCapture();

  public ExtendedAnnotationConfiguration(ExtendedDialect dialect) {
    super(new BootstrapServiceRegistryBuilder().applyIntegrator(METADATA_CAPTURE).build());

    Iterable<? extends BasicType> types = dialect.getExtraTypeOverrides();
    for (BasicType basicType : types) {
      registerTypeOverride(basicType);
    }
  }

  public Map<String, Table> getTableMap() {
    if (METADATA_CAPTURE.getMetadata() == null) {
      throw new IllegalStateException(
          "Cannot access Hibernate Metadata before a SessionFactory has been created");
    }
    Map<String, Table> tables = new HashMap<>();
    for (Table t : METADATA_CAPTURE.getMetadata().collectTableMappings()) {
      tables.put(t.getName(), t);
    }
    return tables;
  }

  public List<AuxiliaryDatabaseObject> getAuxiliaryDatabaseObjects() {
    if (METADATA_CAPTURE.getMetadata() == null) {
      throw new IllegalStateException(
          "Cannot access Hibernate Metadata before a SessionFactory has been created");
    }
    // TODO [SpringHib5] need to figure out how to find the equiv hib5.
    return new ArrayList<>(); // auxiliaryDatabaseObjects;
  }

  public java.util.Collection<PersistentIdentifierGenerator> getGenerators(
      Dialect dialect, String defaultCatalog, String defaultSchema) {
    if (METADATA_CAPTURE.getMetadata() == null) {
      throw new IllegalStateException(
          "Cannot access Hibernate Metadata before a SessionFactory has been created");
    }

    //    TreeMap<Object, PersistentIdentifierGenerator> generators =
    //        new TreeMap<Object, PersistentIdentifierGenerator>();
    //
    //    Iterator<PersistentClass> iter = classes.values().iterator();
    //    while (iter.hasNext()) {
    //      PersistentClass pc = iter.next();
    //
    //      if (!pc.isInherited()) {
    //        IdentifierGenerator ig =
    //            pc.getIdentifier()
    //                .createIdentifierGenerator(
    //                    getIdentifierGeneratorFactory(),
    //                    dialect,
    //                    defaultCatalog,
    //                    defaultSchema,
    //                    (RootClass) pc);
    //
    //        if (ig instanceof PersistentIdentifierGenerator) {
    //          PersistentIdentifierGenerator pig = (PersistentIdentifierGenerator) ig;
    //          generators.put(pig.generatorKey(), pig);
    //        }
    //      }
    //    }
    //
    //    Iterator<Collection> coliter = collections.values().iterator();
    //    while (coliter.hasNext()) {
    //      Collection collection = coliter.next();
    //
    //      if (collection.isIdentified()) {
    //
    //        IdentifierGenerator ig =
    //            ((IdentifierCollection) collection)
    //                .getIdentifier()
    //                .createIdentifierGenerator(
    //                    getIdentifierGeneratorFactory(), dialect, defaultCatalog, defaultSchema,
    // null);
    //
    //        if (ig instanceof PersistentIdentifierGenerator) {
    //          PersistentIdentifierGenerator pig = (PersistentIdentifierGenerator) ig;
    //          generators.put(pig.generatorKey(), pig);
    //        }
    //      }
    //    }
    //
    //    return generators.values();
    // TODO [SpringHib5] need to figure out how to find the equiv hib5.
    return new ArrayList<>();
  }
}
