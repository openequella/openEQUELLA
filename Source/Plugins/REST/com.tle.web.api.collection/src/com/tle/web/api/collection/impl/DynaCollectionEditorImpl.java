package com.tle.web.api.collection.impl;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.DynaCollection;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.guice.BindFactory;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.web.api.baseentity.serializer.AbstractBaseEntityEditor;
import com.tle.web.api.collection.DynaCollectionEditor;
import com.tle.web.api.collection.beans.DynaCollectionBean;

/**
 * @author Aaron
 */
public class DynaCollectionEditorImpl extends AbstractBaseEntityEditor<DynaCollection, DynaCollectionBean>
	implements
		DynaCollectionEditor
{
	@Inject
	private DynaCollectionService dynaCollectionService;

	@AssistedInject
	public DynaCollectionEditorImpl(@Assisted DynaCollection collection,
		@Assisted("stagingUuid") @Nullable String stagingUuid, @Assisted("lockId") @Nullable String lockId,
		@Assisted boolean editing)
	{
		super(collection, stagingUuid, lockId, editing);
	}

	@AssistedInject
	public DynaCollectionEditorImpl(@Assisted DynaCollection collection,
		@Assisted("stagingUuid") @Nullable String stagingUuid)
	{
		this(collection, stagingUuid, null, false);
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
			@Assisted boolean editing);

		DynaCollectionEditorImpl createNewEditor(@Assisted DynaCollection collection,
			@Assisted("stagingUuid") @Nullable String stagingUuid);
	}
}
