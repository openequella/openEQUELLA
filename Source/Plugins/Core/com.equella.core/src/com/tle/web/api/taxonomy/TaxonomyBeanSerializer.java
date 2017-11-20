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

package com.tle.web.api.taxonomy;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.taxonomy.TaxonomyBean;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.web.api.baseentity.serializer.AbstractEquellaBaseEntitySerializer;
import com.tle.web.api.taxonomy.TaxonomyEditorImpl.TaxonomyEditorFactory;

@NonNullByDefault
@Bind
@Singleton
public class TaxonomyBeanSerializer extends AbstractEquellaBaseEntitySerializer<Taxonomy, TaxonomyBean, TaxonomyEditor>
{
	@Inject
	private TaxonomyService taxonomyService;
	@Inject
	private TaxonomyEditorFactory editorFactory;

	@Override
	protected TaxonomyBean createBean()
	{
		return new TaxonomyBean();
	}

	@Override
	protected Taxonomy createEntity()
	{
		return new Taxonomy();
	}

	@Override
	protected TaxonomyEditor createExistingEditor(Taxonomy entity, String stagingUuid, String lockId, boolean importing)
	{
		return editorFactory.createExistingEditor(entity, stagingUuid, lockId, true, importing);
	}

	@Override
	protected TaxonomyEditor createNewEditor(Taxonomy entity, String stagingUuid, boolean importing)
	{
		return editorFactory.createNewEditor(entity, stagingUuid, importing);
	}

	@Override
	protected void copyCustomFields(Taxonomy taxonomy, TaxonomyBean bean, Object data)
	{
		bean.setReadonly(taxonomyService.isTaxonomyReadonly(taxonomy.getUuid()));
	}

	@Override
	protected AbstractEntityService<?, Taxonomy> getEntityService()
	{
		return taxonomyService;
	}

	@Override
	protected Node getNonVirtualNode()
	{
		return Node.TAXONOMY;
	}
}
