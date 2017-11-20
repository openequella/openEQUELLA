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

package com.tle.web.api.collection.impl;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.DynaCollection;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.guice.BindFactory;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.collection.DynaCollectionEditor;
import com.tle.web.api.collection.beans.DynaCollectionBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public class DynaCollectionEditorImpl extends AbstractBaseEntityEditor<DynaCollection, DynaCollectionBean>
	implements
		DynaCollectionEditor
{
	@Inject
	private DynaCollectionService dynaCollectionService;

	@AssistedInject
	public DynaCollectionEditorImpl(@Assisted DynaCollection collection,
		@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
		@Assisted("editing") boolean editing, @Assisted("importing") boolean importing)
	{
		super(collection, stagingUuid, lockId, editing, importing);
	}

	@AssistedInject
	public DynaCollectionEditorImpl(@Assisted DynaCollection collection,
		@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing)
	{
		this(collection, stagingUuid, null, false, importing);
	}

	@Override
	protected void copyCustomFields(DynaCollectionBean bean)
	{
		super.copyCustomFields(bean);

		// FIXME:
	}

	@Override
	protected AbstractEntityService<?, DynaCollection> getEntityService()
	{
		return dynaCollectionService;
	}

	@BindFactory
	public interface DynaCollectionEditorFactory
	{
		DynaCollectionEditorImpl createExistingEditor(@Assisted DynaCollection collection,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
			@Assisted("editing") boolean editing, @Assisted("importing") boolean importing);

		DynaCollectionEditorImpl createNewEditor(@Assisted DynaCollection collection,
			@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("importing") boolean importing);
	}
}
