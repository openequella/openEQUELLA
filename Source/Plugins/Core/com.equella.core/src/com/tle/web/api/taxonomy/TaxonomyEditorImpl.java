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

package com.tle.web.api.taxonomy;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.taxonomy.TaxonomyBean;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.BindFactory;
import com.tle.core.i18n.CoreStrings;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import javax.inject.Inject;

@NonNullByDefault
public class TaxonomyEditorImpl extends AbstractBaseEntityEditor<Taxonomy, TaxonomyBean>
    implements TaxonomyEditor {

  private static final String INTERNAL_DATA_SOURCE_IDENTIFIER = "internal";
  private static final String SQL_DATA_SOURCE_IDENTIFIER = "sql";

  @Inject private TaxonomyService taxonomyService;

  @AssistedInject
  public TaxonomyEditorImpl(
      @Assisted Taxonomy entity,
      @Assisted("stagingUuid") @Nullable String stagingUuid,
      @Assisted("lockId") @Nullable String lockId,
      @Assisted("editing") boolean editing,
      @Assisted("importing") boolean importing) {
    super(entity, stagingUuid, lockId, editing, importing);
  }

  @AssistedInject
  public TaxonomyEditorImpl(
      @Assisted Taxonomy entity,
      @Assisted("stagingUuid") @Nullable String stagingUuid,
      @Assisted("importing") boolean importing) {
    this(entity, stagingUuid, null, false, importing);
  }

  @Override
  protected void copyCustomFields(TaxonomyBean bean) {
    super.copyCustomFields(bean);
    final Taxonomy taxonomy = entity;
    final String dataSource = bean.getDataSource();
    if (dataSource == null || INTERNAL_DATA_SOURCE_IDENTIFIER.equals(dataSource)) {
      taxonomy.setDataSourcePluginId("internalTaxonomyDataSource");
    } else if (SQL_DATA_SOURCE_IDENTIFIER.equals(dataSource)) {
      // Fully supporting this would require copying attributes like the connection string, queries,
      // et al.
      // taxonomy.setDataSourcePluginId("sqlTaxonomyDataSource");
      throw new InvalidDataException(
          new ValidationError(
              "dataSource", CoreStrings.text("api.taxonomy.validation.datasource.notimplemented")));
    } else {
      throw new InvalidDataException(
          new ValidationError(
              "dataSource", CoreStrings.text("api.taxonomy.validation.datasource.unknown")));
    }
  }

  @Override
  protected AbstractEntityService<?, Taxonomy> getEntityService() {
    return taxonomyService;
  }

  @BindFactory
  public interface TaxonomyEditorFactory {
    TaxonomyEditorImpl createExistingEditor(
        Taxonomy taxonomy,
        @Assisted("stagingUuid") @Nullable String stagingUuid,
        @Assisted("lockId") @Nullable String lockId,
        @Assisted("editing") boolean editing,
        @Assisted("importing") boolean importing);

    TaxonomyEditorImpl createNewEditor(
        Taxonomy taxonomy,
        @Assisted("stagingUuid") @Nullable String stagingUuid,
        @Assisted("importing") boolean importing);
  }
}
